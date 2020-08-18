package com.company;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;

public class MultiThread extends Thread {
    private Socket socket;

    public MultiThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {

            // The InetAddress specification
            // to get the IP address of the host
            // getByName() : returns the instance of InetAddress containing LocalHost IP and name
            // InetAddress IA = InetAddress.getByName("localhost");

            // Specify the file
            // Client picks the file from the server
            PickAFile pickAFile = new PickAFile();
            File file = new File(pickAFile.selectFile().toString());
            //****  String to be encrypted is "String str = file.toString()" while will be send on line 36 ****
            // It needs to be encrypted at this step

            String OSystem = findOS();

            FileInputStream fis = new FileInputStream(file);
            long fileLength = file.length();
            String fileLengthString =String.valueOf(fileLength);

            //System.out.println(fileLength);
            // Send File name , OS to client , file length to the client
            byte[] filename = (file.toString()+"#"+OSystem+"#"+fileLengthString).getBytes();
            OutputStream sendFilename =socket.getOutputStream();
            sendFilename.write(filename);
            sendFilename.flush();

            // call function to write content into the file
            writeContent(fileLength , fis);

        } catch(IOException e) {
            System.out.println("Oops: " + e.getMessage());
        }
        finally {
            try {
                socket.close();
            } catch(IOException e) {
                // Oh, well!
            }
        }

    }

    private static String findOS() {
        String os = System.getProperty("os.name");
        return os;
        //System.out.println(System.getProperty("os.name"));
    }

    public void writeContent(long fileLength, FileInputStream fis ) throws IOException {
        //  BufferedInputStream will read the contents from the file in the form of streams
        BufferedInputStream bis = new BufferedInputStream(fis);

        // Get socket's output stream ;
        // OutputStream will write content into the socket for the client
        OutputStream os = socket.getOutputStream();

        // Read File Contents into contents array ;
        // In single write 10K bytes are transferred at a time
        byte[] contents;
        long current = 0;

        while(current!=fileLength){
            int size = 10000;
            if(fileLength - current >= size)
                current += size;
            else{
                size = (int)(fileLength - current);
                current = fileLength;
            }
            contents = new byte[size];
            // read contains : (destination array , begin , end )
            bis.read(contents, 0, size);
            //System.out.println(ThreadColor.ANSI_PURPLE+ current);
            os.write(contents);
            System.out.println(ThreadColor.ANSI_WHITE+"Sending file ... "+(current*100)/fileLength+"% complete!");
        }
        os.flush();
        System.out.println(ThreadColor.ANSI_YELLOW+"File sent succesfully!");
        System.out.println("\n");
    }

}
