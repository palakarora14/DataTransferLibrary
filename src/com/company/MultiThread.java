package com.company;

import com.company.SecurityPackage.AESEncryptionDecryption;
import com.company.SecurityPackage.RandomStringKey;

import java.io.*;
import java.net.Socket;

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

            String OSystem = findOS();

            PickAFile pickAFile = new PickAFile();
            try{
                File file = new File(pickAFile.selectFile().toString());
                // encryption
                // String to be encrypted is "String str = file.toString()"
                int n = 20;
                RandomStringKey randomStringKey = new RandomStringKey();
                String secretKey = randomStringKey.getAlphaNumericString(n);
                System.out.println(secretKey);
                String encryptFile = file.toString();
                AESEncryptionDecryption aesEncryptionDecryption = new AESEncryptionDecryption();
                String encryptedString = aesEncryptionDecryption.encrypt(encryptFile, secretKey);
                System.out.println(ThreadColor.ANSI_GREEN+file.toString());

                FileInputStream fis = new FileInputStream(file);
                long fileLength = file.length();
                String fileLengthString =String.valueOf(fileLength);

                //System.out.println(fileLength);
                // Send File name , OS to client , file length to the client
                byte[] filename = (encryptedString+"#"+OSystem+"#"+fileLengthString+"#"+secretKey).getBytes();
                OutputStream sendFilename =socket.getOutputStream();
                sendFilename.write(filename);
                sendFilename.flush();

                // call function to write content into the file
                sendContent(fileLength , fis);

            }
            catch (NullPointerException e){
                byte[] filename = ("No String"+"#"+OSystem+"#"+"0"+"#"+"zero").getBytes();
                OutputStream sendFilename =socket.getOutputStream();
                sendFilename.write(filename);
                sendFilename.flush();
            }

        } catch(IOException  e) {
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

    public void sendContent(long fileLength, FileInputStream fis ) throws IOException {
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
            int size = 500000;
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
            try {
                os.write(contents);
            }catch (IOException e){
                System.out.println("Client closed");
                break;
            }
            System.out.println(ThreadColor.ANSI_WHITE+"Sending file ... "+(float)(current*100)/fileLength+"% complete!");

        }
        os.flush();
        if(current == fileLength){
        System.out.println(ThreadColor.ANSI_YELLOW+"File sent succesfully!");
        System.out.println("\n");}
        else{
            System.out.println(ThreadColor.ANSI_YELLOW+"Only "+(float)(current*100)/fileLength+"% file send was complete!");
        }
    }

}
