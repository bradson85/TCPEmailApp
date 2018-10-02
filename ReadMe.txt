-Brad Peradotto
-Cs 447
-Project 3

Directions to run Project 3

Coded in java


To Compile: ************************************************************************************************
*
* 1. Once you extract PR03 from the tar file run the makefile ‘make’ 
*    (this runs 3 other make files in respective subfolders)
*      
* 2. This should produce 3 .jar executables:
*    a. server.jar
*    b. client.jar
*    c. keygen.jar  (OPTIONAL)
*****************************************************************************************

To Run:
***********************************************************************************************
* 1.  To Run the smtp server with TLS 
*      type:“java -jar server.jar <tcp-listen-port>”   (without  brackets) 
*
* 2. To Run the client Sender type: “java -jar sender.jar <HOST-Name> <PORT>”.     (without  brackets) 
*
***********************************************************************************************
*  KEYGENERATOR: (Optional)
* 3. I have also included a key and certificate generator. THIS is optional. I have already   
*    included the key, certificate, and truststore files in the tar. This program has a prompt that asks for which side client or server.
         ***** WARNING DO NOT Run keygen.jar without understanding that you have to copy each others truststore.certs file into 
*               each others proper location ************  
* Must do the following if you use the keygen.jar program:
*   a. When run on the Server side a file will be made called client_truststore.certs this needs to be copied inside of   
*     the client’s “db” folder.
*   b. When run on the Client side a file will be made called server_truststore.certs this needs to be copied inside of 
*    the server’s “db” folder.

* To Run the client Receiver type:“java -jar keyegen”.   
*   Follow the prompt.
*
********************************************************************************************************************************
