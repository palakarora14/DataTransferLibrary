package com.company;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransferServer {

    public static void main(String[] args) throws Exception {
        // Initialize Sockets
        ServerSocket ssock = new ServerSocket(5000);

        while(true){
            new MultiThread(ssock.accept()).start();
            System.out.println(ThreadColor.ANSI_WHITE+"Client Connected");

        }

    }
}
