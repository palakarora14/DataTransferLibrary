package com.company;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

public class FileTransferServer {

    public static void main(String[] args) throws Exception {

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



        // Initialize Sockets
        ServerSocket ssock = new ServerSocket(5000);


        while(true){
            new MultiThread(ssock.accept()).start();
            System.out.println(ThreadColor.ANSI_WHITE+"Client Connected");

        }

    }
}
