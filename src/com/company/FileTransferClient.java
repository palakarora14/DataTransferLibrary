package com.company;

import com.company.SecurityPackage.AESEncryptionDecryption;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.stream.Stream;


public class FileTransferClient {

    // copy the "System IP Address : " obtained after running the FileTransferServer.java file
    private static final String serverIp="192.168.43.74";
    private static FileWriter fw;
    private static FileWriter fwLog;
    private static File f;

    public static void main(String[] args) throws Exception {

        String os = findOS();
        String home = System.getProperty("user.home");
        if (os.contains("Mac")) {
            fw=new FileWriter(home + "/Downloads/GoodProgram/clientConfig.ini");
            fwLog=new FileWriter(home + "/Downloads/GoodProgram/clientLog.txt");
            f = new File(home + "/Downloads/GoodProgram/clientLog.txt");
        } else {
            //C:\Users\hp
            fw=new FileWriter(home + "\\Downloads\\GoodProgram\\clientConfig.ini");
            fwLog=new FileWriter(home + "\\Downloads\\GoodProgram\\clientLog.txt");
            f = new File(home + "\\Downloads\\GoodProgram\\clientLog.txt");
        }
        fw.write("THIS IS THE CLIENT CONFIGURATION FILE" + "\n"+"\n");
        fw.write("Client connected @Port = 5000"+"\n");
        fwLog.write("THIS IS THE CLIENT LOG FILE" + "\n"+"\n");
        System.out.println("CLIENT CONFIGURATION FILE CREATED ");
        System.out.println("CLIENT LOG FILE CREATED ");

        writeConfigurationFile(fw ,"Server Ip = "+serverIp);
        writeConfigurationFile(fw ,"Client Os = "+os);

        Scanner scanner= new Scanner(System.in);
        System.out.println("You want to Download or Upload content ? ");
        System.out.println("Enter [D/U] : ");
        String str= scanner.nextLine();

        switch (str.toLowerCase()){
            case "d" :
                System.out.println("\"Select a file from the server\"\n");
                writeConfigurationFile(fw,"Client will Download file from the server");
                writeConfigurationFile(fw,"Client log filePath = "+f.getAbsolutePath());
                download();
                break;
            case "u" :
                System.out.println("If you want to upload file to the server :\n Run FileTransferServer.java on Client side \"sender\" " +
                        "and FileTransferClient.java on server side \"receiver\" \n Because here server can only send & client can only download file " +
                        "\n Thanks ! ");
                writeConfigurationFile(fw,"Client want to upload file to the server");
                // upload ;
                break;
            default :
                System.out.println("Please Enter either [D/U]");
                writeConfigurationFile(fw,"Client added wrong choice");
        }
    }

    private static void download() throws Exception{
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

            writeLogFile(fwLog,"[Receive details from server]");
            if(filename.equals("No String")){
                System.out.println("No file was selected !");
                writeLogFile(fwLog,"No file was selected");
            }
            else{
                writeLogFile(fwLog,"encrypted file = "+filename+" \n Os of server = "+serverOperatingSystem+
                        " \n fileLength = "+fileLength+"\n");
                writeLogFile(fwLog,"[Decryption]");
                writeLogFile(fwLog," secret key = "+secretKey);

                // decryption
                // String to be decrypted is "String str = filename"
                AESEncryptionDecryption aesEncryptionDecryption = new AESEncryptionDecryption();
                String decryptedString = aesEncryptionDecryption.decrypt(filename, secretKey);
                System.out.println(ThreadColor.ANSI_GREEN+decryptedString);
                writeLogFile(fwLog," File - Secret key = "+decryptedString+"\n");

                //check for OS of client and server
                String ClientOS = findOS();
                String target;
                String Fname;
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
                writeLogFile(fwLog,"[Download file from the server]");
                Files.createDirectories(Paths.get(target));
                writeLogFile(fwLog,"Directory for download = "+Paths.get(target));
                if (ServerOSMac) {
                    // find out the name of the file to be downloaded from the server from complete filePath
                    Fname = decryptedString.substring(decryptedString.lastIndexOf("/") + 1);
                } else {
                    Fname = decryptedString.substring(decryptedString.lastIndexOf("\\") + 1);
                }
                writeLogFile(fwLog,"File to be download = "+Fname);
                // Remove the null character from the string
                StringJoiner joiner = new StringJoiner("");
                Stream.of(Fname.split("\0")).forEach(joiner::add);
                Path pathToFile = Paths.get(target + joiner);
                writeLogFile(fwLog,"Create filepath to download file = "+pathToFile+"\n");


                // Initialize the FileOutputStream to the output file's full path
                FileOutputStream fos = new FileOutputStream(String.valueOf(pathToFile));

                // BufferedOutputStream will write the contents to the file
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                // InputStream will read content from the socket for the client
                InputStream is = socket.getInputStream();


                int fileLengthLong = Integer.parseInt(fileLength);

                writeLogFile(fwLog,"[Start reading content from client and write to download file]");

                long current = downloadContent(is,contents,bos,fileLengthLong,socket);

                if(current == fileLengthLong) {
                    bos.flush();

                    System.out.print(Fname + " : ");
                    System.out.println( ThreadColor.ANSI_YELLOW +"File saved successfully!");
                    writeLogFile(fwLog,"\n[Complete]");
                    writeLogFile(fwLog,"File saved successfully!");
                    //Italicized text
                    System.out.println(ThreadColor.ANSI_BLUE+ "\n"+"\033[3mRefer GoodProgram folder in your Downloads folder\033[0m");
                    writeLogFile(fwLog,"Refer GoodProgram folder in your Downloads folder to find the downloaded file");
                    fw.close();
                    socket.close();
                    // break;
                }
                else {
                    System.out.println(ThreadColor.ANSI_YELLOW + "Server Disconnected");
                    writeLogFile(fwLog,"Error = Server Disconnected");
                }
            }

        }catch(ConnectException e){
            System.out.println("Server not Connected !");
            writeLogFile(fwLog,"Error = Server not Connected");
        }
    }

    public static long downloadContent(InputStream is,byte[] contents,BufferedOutputStream bos,int fileLengthLong,Socket socket) throws Exception{
        //No of bytes read in one read() call
        int bytesRead;
        long current = 0;
        long total=0;
        long num=0;
        long start = System.currentTimeMillis();
        //System.out.println(start);
        try{
            // returns the number of bytes read, or -1 if the end of the stream has been reached.
            writeLogFile(fwLog,"Starting receiving content of the file from the server");
            while ((bytesRead = is.read(contents)) != -1) {
                boolean flag = hostAvailabilityCheck();
                //                       System.out.println(flag);
                if(flag) {
                    writeLogFile(fwLog,"Host Available");
                    current += bytesRead;
                    writeLogFile(fwLog,"Bytes read = "+current);
                    bos.write(contents, 0, bytesRead);
                    writeLogFile(fwLog,"write to destination");

                    long speed = printSpeed(start,current);
                    writeLogFile(fwLog,"Speed of transaction = "+speed);
                    total+=speed;
                    num++;
                    //                           System.out.println(current);
                    System.out.println(ThreadColor.ANSI_WHITE+"Downloading file ... "+(float)(current*100)/fileLengthLong+"% complete!");
                    writeLogFile(fwLog,"Downloading file ... "+(float)(current*100)/fileLengthLong+"% complete!\n");
                }
                else{
                    // for broken connection via server socket
                    writeLogFile(fwLog,"Host Unavailable");
                    System.out.println(ThreadColor.ANSI_PURPLE+ "Server connection Interrupted ! ");
                    System.out.println(ThreadColor.ANSI_PURPLE+ "Waiting for Connection");
                    //                           System.out.println(current+"**");
                    writeLogFile(fwLog,"Server connection Interrupted");
                    writeLogFile(fwLog,"Waiting for Connection");
                    writeLogFile(fwLog,"Let the thread sleep for 8 seconds");

                    Thread.sleep(8000);
                    // System.out.println("Thread is up");
                    flag = hostAvailabilityCheck();
                    writeLogFile(fwLog,"Thread is up check for connection");
                    if(flag)
                    {
                        System.out.println("File can be retrieved");
                        writeLogFile(fwLog,"Host available , file can be retrieved");
                    }
                    else{
                        System.out.println("File can't be retrieved");
                        writeLogFile(fwLog,"Host still unavailable , file can't be retrieved");
                        System.out.println(ThreadColor.ANSI_WHITE+(float)(current*100)/fileLengthLong+"% of the file was downloaded though !");
                        System.out.println(ThreadColor.ANSI_BLUE+ "\n"+"\033[3mRefer GoodProgram folder in your Downloads folder\033[0m");
                        writeLogFile(fwLog,"(float)(current*100)/fileLengthLong+\"% of the file was downloaded though ");
                        writeLogFile(fwLog,"Refer GoodProgram folder in your Downloads folder to find file");
                        socket.close();

                    }
                }
            }
            // find average speed
            long average = total/num;
            System.out.println("\nAverage speed achieved in the whole transaction is : "+average+" MB/s\n");
            writeLogFile(fwLog,"\n"+"[Average speed]");
            writeLogFile(fwLog,"Average speed achieved in the whole transaction is : "+average+" MB/s");
        }
        catch (SocketException e){
            System.out.println(ThreadColor.ANSI_YELLOW+ "Oops Server Disconnected , Try after some Time !!");
            writeLogFile(fwLog,"Error = Server Disconnected");
        }
        return current;
    }

    private static String findOS() {
        return System.getProperty("os.name");
        //System.out.println(System.getProperty("os.name"));
    }

    private static boolean hostAvailabilityCheck() throws Exception{
        return InetAddress.getByName(serverIp).isReachable(10000);
    }

    public static long printSpeed(long start , long current) {
    long speed = 0;
            long cost = System.currentTimeMillis() - start;
        //System.out.println(cost);
            try{speed= current/cost/1000;
                System.out.printf(ThreadColor.ANSI_PURPLE + "Read %,d bytes, speed: %,d MB/s%n", current, speed);
                }
            catch (ArithmeticException e){
                System.out.println("File transferred in no time ");
            }
        return speed;
    }

    private static void writeConfigurationFile(FileWriter fw, String str) {
        try{
            fw.write(str +"\n");
            fw.flush();
            //fw.close();
        }catch(Exception e)
        {System.out.println(e.getMessage());}

    }

    private static void writeLogFile(FileWriter fwLog, String str) {
        try{
            fwLog.write(str +"\n");
            fwLog.flush();
            //fwLog.close();
        }catch(Exception e)
        {System.out.println(e.getMessage());}
    }
}
