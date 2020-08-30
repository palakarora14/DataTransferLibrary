# DataTransferLibrary

* Data Transfer Library is a Java based console application which uses a server client based program that communicates via Sockets and is capable of transferring        any content across multiple machines or the same machine.
* Multiple content as it can accept and transfer any kind of file extension i.e text, PDF , MP4 , png/jpeg , cpp , java , exe , zip etc.
* For Data transfer we have used a server program and client program in which the server can send the data and the client can download the same data .
* The library will work very efficiently if both the client and the server program are working on the same computer but if multiple machine transfers are happening that is we are using two different computers then both of these computers need to be on the same network for the library to work.
* Features : The library is a high speed, real-time, scalable, secure, reliable data transfer library which can be used by cross platform applications.
* can run on both Mac & Windows based system
* downloaded file will have the same name as was in server
* uses AES for encryption and decryption
* works in case of connection loss or slow connection by server or client

*Run :*
1. The system that wants to send the file should run FileTransferServer.java the console will display the Ip address of the server needs to pass this to the client
2. The client side should run FileTransferClient.java with the server IP received from the server
3. In server system then select a file to be transferred
4. Then the file will be Downloaded on the client system

*Complete Documentation containing user manual , design description , features provided , Test & Results etc. is available in file "Documentation of DataTransferLibrary.pdf"*


