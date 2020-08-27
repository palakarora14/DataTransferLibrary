package com.company;

import java.io.*;
import java.net.*;

public class FileTransferServer {

    public static void main(String[] args) throws Exception {

        String Os = findOs();
        String home = System.getProperty("user.home");
        FileWriter fwLog;
        File f;
        FileWriter fw;
        if (Os.contains("Mac")) {
            fw =new FileWriter(home + "/Downloads/GoodProgram/serverConfig.ini");
            fwLog =new FileWriter(home + "/Downloads/GoodProgram/serverLog.txt");
            f = new File(home + "/Downloads/GoodProgram/serverLog.txt");
        } else {
            //C:\Users\hp
            fw =new FileWriter(home + "\\Downloads\\GoodProgram\\serverConfig.txt");
            fwLog =new FileWriter(home + "\\Downloads\\GoodProgram\\serverLog.txt");
            f = new File(home + "\\Downloads\\GoodProgram\\serverLog.txt");
        }
        fw.write("THIS IS THE SERVER CONFIGURATION FILE" + "\n"+"\n");
        fw.write("Server is listening @Port = 5000"+"\n");
        fwLog.write("THIS IS THE SERVER LOG FILE" + "\n"+"\n");
        System.out.println("SERVER CONFIGURATION FILE CREATED ");
        System.out.println("SERVER LOG FILE CREATED ");

        String ip = findIp();
        writeConfigurationFile(fw,"Server Os = "+ Os);
        writeConfigurationFile(fw,"Server Ip = "+ ip);

        // Initialize Sockets
        ServerSocket ssock = new ServerSocket(5000);
        if(ssock.isClosed())
        {
            System.out.println("Client socket closed");
            writeConfigurationFile(fw,"Client socket closed");
        }

        while(true){
            new MultiThread(ssock.accept(), fwLog).start();
            System.out.println(ThreadColor.ANSI_WHITE+"Client Connected");
            writeConfigurationFile(fw,"Client Connected");
            writeConfigurationFile(fw,"Server log filePath = "+ f.getAbsolutePath());
        }
    }

    private static String findIp() throws UnknownHostException {
        //get the ipaddress of the system
        //local Ip
        InetAddress localhost = InetAddress.getLocalHost();
        String privateIP = (localhost.getHostAddress()).trim();

        System.out.println("System IP Address : " + privateIP);

        return  privateIP;
        // Find public IP address
//        String systemipaddress;
//        try
//        {
//            URL url_name = new URL("http://bot.whatismyipaddress.com");
//            BufferedReader sc =
//                    new BufferedReader(new InputStreamReader(url_name.openStream()));
//            // reads system IPAddress
//            systemipaddress = sc.readLine().trim();
//        }
//        catch (Exception e)
//        {
//            systemipaddress = "Cannot Execute Properly";
//        }
//        System.out.println("Public IP Address: " + systemipaddress +"\n");
    }

    private static String findOs() {
        return System.getProperty("os.name");
    }

    private static void writeConfigurationFile(FileWriter fw, String str) {
        try{
            fw.write(str +"\n");
            fw.flush();
            //fw.close();
        }catch(Exception e)
        {System.out.println(e.getMessage());}

    }

}
