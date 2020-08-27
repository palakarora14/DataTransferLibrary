package com.company;

import com.company.SecurityPackage.AESEncryptionDecryption;
import com.company.SecurityPackage.RandomStringKey;

import java.io.*;
import java.net.Socket;

public class MultiThread extends Thread {
    private Socket socket;
    private FileWriter fwLog;

    public MultiThread(Socket socket,FileWriter fwLog) {
        this.socket = socket;
        this.fwLog =fwLog;
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
                writeLogFile(fwLog,"[File selected]");
                writeLogFile(fwLog,"FilePath of the file to be send from server = "+file.toString()+" of length = "+file.length()+"\n");
                writeLogFile(fwLog,"[Encryption]");
                // encryption
                // String to be encrypted is "String str = file.toString()"
                int n = 20;
                RandomStringKey randomStringKey = new RandomStringKey();
                String secretKey = randomStringKey.getAlphaNumericString(n);
                writeLogFile(fwLog,"Secret key of size 20 = "+secretKey);
                String encryptFile = file.toString();
                AESEncryptionDecryption aesEncryptionDecryption = new AESEncryptionDecryption();
                String encryptedString = aesEncryptionDecryption.encrypt(encryptFile, secretKey);
                System.out.println(ThreadColor.ANSI_GREEN+file.toString());
                writeLogFile(fwLog,"File + Secret key = "+encryptedString+"\n");


                FileInputStream fis = new FileInputStream(file);
                long fileLength = file.length();
                String fileLengthString =String.valueOf(fileLength);

                //System.out.println(fileLength);
                // Send File name , OS to client , file length to the client
                byte[] filename = (encryptedString+"#"+OSystem+"#"+fileLengthString+"#"+secretKey).getBytes();
                OutputStream sendFilename =socket.getOutputStream();
                sendFilename.write(filename);
                sendFilename.flush();
                writeLogFile(fwLog,"[Send details to client]");
                writeLogFile(fwLog,"Encrypted filename + Os of server + fileLength + secret key for encryption is send to the client"+"\n");
                writeLogFile(fwLog,"[Read selected file and start sending it to client]");

                // call function to read content from the file and send it to client
                sendContent(fileLength , fis);

            }
            catch (NullPointerException e){
                writeLogFile(fwLog,"[No file selected]");
                byte[] filename = ("No String"+"#"+OSystem+"#"+"0"+"#"+"zero").getBytes();
                OutputStream sendFilename =socket.getOutputStream();
                sendFilename.write(filename);
                sendFilename.flush();
                writeLogFile(fwLog,"tell no file selected to client ");
            }

        } catch(IOException  e) {
            System.out.println("Oops: " + e.getMessage());
            writeLogFile(fwLog,"Error = " + e.getMessage());
        }
        finally {
            try {
                socket.close();
            } catch(IOException e) {
                // Oh, well!
            }
        }

    }

    private void writeLogFile(FileWriter fwLog, String str) {
        try{
            fwLog.write(str +"\n");
            fwLog.flush();
            //fwLog.close();
        }catch(Exception e)
        {System.out.println(e);}
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

        writeLogFile(fwLog,"Starting Reading from the beginning of the file");
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
            writeLogFile(fwLog,"Bytes read = "+ current);
            //System.out.println(ThreadColor.ANSI_PURPLE+ current);
            try {
                os.write(contents);
                writeLogFile(fwLog,"write to client");
            }catch (IOException e){
                System.out.println("Client closed");
                writeLogFile(fwLog,"Error = Client closed");
                break;
            }
            System.out.println(ThreadColor.ANSI_WHITE+"Sending file ... "+(float)(current*100)/fileLength+"% complete!");
            writeLogFile(fwLog,"Sending file ... "+(float)(current*100)/fileLength+"% complete!"+"\n");

        }
        os.flush();
        if(current == fileLength){
        System.out.println(ThreadColor.ANSI_YELLOW+"File sent successfully!");
        System.out.println("\n");
            writeLogFile(fwLog,"[Complete]");
            writeLogFile(fwLog,"File sent successfully!");
        }
        else{
            System.out.println(ThreadColor.ANSI_YELLOW+"Only "+(float)(current*100)/fileLength+"% file send was complete!");
            writeLogFile(fwLog,"Only "+(float)(current*100)/fileLength+"% file send was complete!");
        }
    }

}
