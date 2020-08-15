package com.company;

import java.io.*;
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
    private static String serverIp="";

    public static void main(String[] args) throws Exception{

        //Initialize socket
        BufferedReader inFromUser =new BufferedReader(new   InputStreamReader(System.in));
        //Socket socket = new Socket(InetAddress.getByName("localhost"), 5000);
        Socket socket = new Socket(serverIp, 5000);
        byte[] contents = new byte[10000];

        // Read filename and OS from server
        byte[] filenameOSByteArray = new byte[100];
        InputStream receiveFilenameOS = socket.getInputStream();
        receiveFilenameOS.read(filenameOSByteArray);
        String filenameOS = new String(filenameOSByteArray);
        // divide filenameOS into filename and OS
        String[] split = filenameOS.split("#");
        String filename = split[0] ;
        String serverOperatingSystem = split[1] ;
        //System.out.println(filename+" "+serverOperatingSystem);

        //check for OS of client and server
        String target;
        String Fname;
        String ClientOS = findOS();
        boolean ClientOSMac = ClientOS.contains("Mac"); //true
        boolean ServerOSMac = serverOperatingSystem.contains("Mac"); //true

        // set home directory of user to "home"
        String home = System.getProperty("user.home");
        if(ClientOSMac){
            target = home+"/Downloads/GoodProgram/";
        }else{
            //C:\Users\hp
            target = home+"\\Downloads\\GoodProgram\\";
        }
        // Create Directory
        Files.createDirectories(Paths.get(target));
        if(ServerOSMac){
            // find out the name of the file to be downloaded from the server from complete filePath
            Fname = filename.substring(filename.lastIndexOf("/") + 1);
        }else{
            Fname = filename.substring(filename.lastIndexOf("\\") + 1);
        }

        // Remove the null character from the string
        StringJoiner joiner = new StringJoiner("");
        Stream.of(Fname.split("\0")).forEach(joiner::add);
        Path pathToFile = Paths.get(target+ joiner);

        // Initialize the FileOutputStream to the output file's full path
        FileOutputStream fos = new FileOutputStream(String.valueOf(pathToFile));

        // BufferedOutputStream will write the contents to the file
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        // InputStream will read content from the socket for the client
        InputStream is = socket.getInputStream();

        //No of bytes read in one read() call
        int bytesRead = 0;

        // returns the number of bytes read, or -1 if the end of the stream has been reached.
        while((bytesRead=is.read(contents))!=-1)
            bos.write(contents, 0, bytesRead);

        bos.flush();
        socket.close();

        System.out.print(Fname + " : ");
        System.out.println( ThreadColor.ANSI_YELLOW +"File saved successfully!");
        //Italicized text
        System.out.println(ThreadColor.ANSI_BLUE+ "\n"+"\033[3mRefer GoodProgram folder in your Downloads folder\033[0m");
    }

    private static String findOS() {
        String os = System.getProperty("os.name");
        return os;
        //System.out.println(System.getProperty("os.name"));
    }
}
