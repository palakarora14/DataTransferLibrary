package com.company;

import com.company.SecurityPackage.AESEncryptionDecryption;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;
import java.util.stream.Stream;


public class FileTransferClient {

    // copy the "System IP Address : " obtained after running the FileTransferServer.java file
    private static final String serverIp="";

    public static void main(String[] args) throws Exception{

            try {
                //Initialize socket
                Socket socket = new Socket(serverIp, 5000);

                byte[] contents = new byte[500000];

                // Read filename , OS , fileLength  from server
                byte[] filenameOSByteArray = new byte[200];
                InputStream receiveFilenameOS = socket.getInputStream();
                receiveFilenameOS.read(filenameOSByteArray);
                String filenameOS = new String(filenameOSByteArray);
                // divide filenameOS into filename and OS
                String[] split = filenameOS.split("#");
                String filename = split[0];
                String serverOperatingSystem = split[1];
                String fileLength = split[2].trim();
                String secretKey = split[3].trim();
                if(filename.equals("No String")){
                    System.out.println("No file was selected !");
                }
                else{
                    // decryption
                    // String to be decrypted is "String str = filename"
                    AESEncryptionDecryption aesEncryptionDecryption = new AESEncryptionDecryption();
                    String decryptedString = aesEncryptionDecryption.decrypt(filename, secretKey);
                    System.out.println(ThreadColor.ANSI_GREEN+decryptedString);

                    //check for OS of client and server
                    String target;
                    String Fname;
                    String ClientOS = findOS();
                    boolean ClientOSMac = ClientOS.contains("Mac"); //true
                    boolean ServerOSMac = serverOperatingSystem.contains("Mac"); //true

                    // set home directory of user to "home"
                    String home = System.getProperty("user.home");
                    if (ClientOSMac) {
                        target = home + "/Downloads/GoodProgram/";
                    } else {
                        //C:\Users\hp
                        target = home + "\\Downloads\\GoodProgram\\";
                    }
                    // Create Directory
                    Files.createDirectories(Paths.get(target));
                    if (ServerOSMac) {
                        // find out the name of the file to be downloaded from the server from complete filePath
                        Fname = decryptedString.substring(decryptedString.lastIndexOf("/") + 1);
                    } else {
                        Fname = decryptedString.substring(decryptedString.lastIndexOf("\\") + 1);
                    }

                    // Remove the null character from the string
                    StringJoiner joiner = new StringJoiner("");
                    Stream.of(Fname.split("\0")).forEach(joiner::add);
                    Path pathToFile = Paths.get(target + joiner);


                    // Initialize the FileOutputStream to the output file's full path
                    FileOutputStream fos = new FileOutputStream(String.valueOf(pathToFile));

                    // BufferedOutputStream will write the contents to the file
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    // InputStream will read content from the socket for the client
                    InputStream is = socket.getInputStream();


                    int fileLengthLong = Integer.parseInt(fileLength);

                    long current = downloadContent(is,contents,bos,fileLengthLong,socket);

                    if(current == fileLengthLong) {
                        bos.flush();

                        System.out.print(Fname + " : ");
                        System.out.println( ThreadColor.ANSI_YELLOW +"File saved successfully!");
                        //Italicized text
                        System.out.println(ThreadColor.ANSI_BLUE+ "\n"+"\033[3mRefer GoodProgram folder in your Downloads folder\033[0m");
                        socket.close();
                        // break;
                    }
                    else {
                        System.out.println(ThreadColor.ANSI_YELLOW + "Server Disconnected");
                    }
                }

            }catch(ConnectException e){
                System.out.println("Server not Connected !");
            }

    }

    private static String findOS() {
        String os = System.getProperty("os.name");
        return os;
        //System.out.println(System.getProperty("os.name"));
    }

    private static boolean hostAvailabilityCheck() throws Exception{
        boolean temp = InetAddress.getByName(serverIp).isReachable(10000);
        return temp;
    }

    public static long downloadContent(InputStream is,byte[] contents,BufferedOutputStream bos,int fileLengthLong,Socket socket) throws Exception{
        //No of bytes read in one read() call
        int bytesRead = 0;
        long current = 0;
        long total=0;
        long num=0;
        long start = System.currentTimeMillis();
        //System.out.println(start);
        try{
            // returns the number of bytes read, or -1 if the end of the stream has been reached.
            while ((bytesRead = is.read(contents)) != -1) {
                boolean flag = hostAvailabilityCheck();
                //                       System.out.println(flag);
                if(flag) {
                    current += bytesRead;
                    bos.write(contents, 0, bytesRead);

                    long speed = printSpeed(start,current);
                    total+=speed;
                    num++;
                    //                           System.out.println(current);
                    System.out.println(ThreadColor.ANSI_WHITE+"Downloading file ... "+(float)(current*100)/fileLengthLong+"% complete!");
                }
                else{
                    // for broken connection via server socket
                    System.out.println(ThreadColor.ANSI_PURPLE+ "Server connection Interrupted ! ");
                    System.out.println(ThreadColor.ANSI_PURPLE+ "Waiting for Connection");
                    //                           System.out.println(current+"**");

                    Thread.sleep(8000);
                    // System.out.println("Thread is up");
                    flag = hostAvailabilityCheck();
                    if(flag)
                    {
                        System.out.println("File can be retrieved");
                    }
                    else{
                        System.out.println("File can't be retrieved");
                        System.out.println(ThreadColor.ANSI_WHITE+(float)(current*100)/fileLengthLong+"% of the file was downloaded though !");
                        System.out.println(ThreadColor.ANSI_BLUE+ "\n"+"\033[3mRefer GoodProgram folder in your Downloads folder\033[0m");
                        socket.close();

                    }
                }
            }
            // find average speed
            long average = total/num;

            System.out.println("\nAverage speed achieved in the whole transaction is : "+average+" MB/s\n");
        }
        catch (SocketException e){
            System.out.println(ThreadColor.ANSI_YELLOW+ "Oops Server Disconnected , Try after some Time !!");
        }
        return current;
    }

    public static long printSpeed(long start , long current) {
    long speed = 0;
            long cost = System.currentTimeMillis() - start;
        //System.out.println(cost);
            try{speed= current/cost/1000;
                System.out.printf(ThreadColor.ANSI_PURPLE + "Read %,d bytes, speed: %,d MB/s%n", current, speed);
                }
            catch (ArithmeticException e){
                System.out.println(start+" "+cost);
            }
        return speed;
    }
}
