package com.company;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;
import java.util.stream.Stream;


public class FileTransferClient {

    // copy the "System IP Address : " obtained after running the FileTransferServer.java file
    private static String serverIp="192.168.43.25";

    public static void main(String[] args) throws Exception{

            try {
                //Initialize socket
                Socket socket = new Socket(serverIp, 5000);

                byte[] contents = new byte[10000];

                // Read filename , OS , fileLength  from server
                byte[] filenameOSByteArray = new byte[100];
                InputStream receiveFilenameOS = socket.getInputStream();
                receiveFilenameOS.read(filenameOSByteArray);
                String filenameOS = new String(filenameOSByteArray);
                // divide filenameOS into filename and OS
                String[] split = filenameOS.split("#");
                String filename = split[0];
                String serverOperatingSystem = split[1];
                String fileLength = split[2];
                //System.out.println(filename+" "+serverOperatingSystem+ " "+fileLength);

                //****  String to be decrypted is "String str = filename" ****
                // It needs to be decrypted at this step

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
                    Fname = filename.substring(filename.lastIndexOf("/") + 1);
                } else {
                    Fname = filename.substring(filename.lastIndexOf("\\") + 1);
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

                //No of bytes read in one read() call
                int bytesRead = 0;
                long current = 0;
                int fileLengthLong = Integer.parseInt(fileLength.trim());

                // returns the number of bytes read, or -1 if the end of the stream has been reached.
                while ((bytesRead = is.read(contents)) != -1) {
                    boolean flag = hostAvailabilityCheck();
                    System.out.println(ThreadColor.ANSI_WHITE+flag);
                    if(flag) {
                        current += bytesRead;
                        bos.write(contents, 0, bytesRead);
                        System.out.println(ThreadColor.ANSI_WHITE+current);
                    }
                    else{
                        // for broken connection via server socket
                        System.out.println(ThreadColor.ANSI_PURPLE+ "Server connection Interrupted ! ");
                        System.out.println(ThreadColor.ANSI_PURPLE+ "Waiting for Connection");
                        System.out.println(current+"**");
                        is.close();

                        Thread.sleep(8000);
                        // System.out.println("Thread is up");
                        flag = hostAvailabilityCheck();
                        if(flag)
                        {
                            // file can be retrieved
                        System.out.println("File can be retrieved");
                        //request file from the position after current from the server(Sending)
                            byte[] fileCurrent = (filename+"#"+current+"#"+fileLength).getBytes();
                            OutputStream sendFileCurrent =socket.getOutputStream();
                            sendFileCurrent.write(fileCurrent);
                            sendFileCurrent.flush();

                        //InputStream inputStream = socket.getInputStream();
                        break;
                        }
                        else{
                            System.out.println("File can't be retrieved");
                        System.out.println(ThreadColor.ANSI_WHITE+(current*100)/fileLengthLong+"% of the file was downloaded though !");
                        System.out.println(ThreadColor.ANSI_BLUE+ "\n"+"\033[3mRefer GoodProgram folder in your Downloads folder\033[0m");
                        socket.close();
                        break;
                        }
                    }
                }
                if(current == fileLengthLong) {
                    bos.flush();

                    System.out.print(Fname + " : ");
                    System.out.println( ThreadColor.ANSI_YELLOW +"File saved successfully!");
                    //Italicized text
                    System.out.println(ThreadColor.ANSI_BLUE+ "\n"+"\033[3mRefer GoodProgram folder in your Downloads folder\033[0m");
                    socket.close();
                   // break;
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
}
