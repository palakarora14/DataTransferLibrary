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

    public static void main(String[] args) throws Exception{

        //get the ipaddress of the system
        //local Ip
        InetAddress localhost = InetAddress.getLocalHost();
        String privateIP = (localhost.getHostAddress()).trim();
        System.out.println("System IP Address : " + privateIP);
        // Find public IP address
        String systemipaddress;
        try
        {
            URL url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc =
                    new BufferedReader(new InputStreamReader(url_name.openStream()));
            // reads system IPAddress
            systemipaddress = sc.readLine().trim();
        }
        catch (Exception e)
        {
            systemipaddress = "Cannot Execute Properly";
        }
        System.out.println("Public IP Address: " + systemipaddress +"\n");




        //Initialize socket
        //Socket socket = new Socket(InetAddress.getByName("localhost"), 5000);
        Socket socket = new Socket(privateIP, 5000);
        byte[] contents = new byte[10000];

        // Read filename from server
        byte[] filenameByteArray = new byte[100];
        InputStream receiveFilename = socket.getInputStream();
        receiveFilename.read(filenameByteArray);
        String filename = new String(filenameByteArray);

        // Create Directory
        String target = "/Users/aroras/Downloads/GoodProgram/";
        Files.createDirectories(Paths.get(target));
        // find out the name of the file to be downloaded from the server from complete filePath
        String Fname = filename.substring(filename.lastIndexOf("/") + 1);

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

        System.out.println("File saved successfully!");
    }
}
