/*
 * Bradley Peradotto
 * Last Edit 6/26/17
 *
 * This is the code for managing the messages needed to send to the server from the client.
 * It allows the client to enter data through the system scanner and send it to the server.
 */
package emailclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;


public class ClientSend implements Runnable {

    //variables
    private Socket socket;
    
// constructor
    public ClientSend(SSLSocket socket) {
        this.socket = socket;
       
    }
 // main area for handling messages
    @Override
    public void run() {
        
       
        BufferedReader in = null;
        PrintStream out =null;
        String temp;
        String request;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintStream(socket.getOutputStream(), true);
              out.println(encode(" "));
            } catch (IOException ex) {
                Logger.getLogger(ClientSend.class.getName()).log(Level.SEVERE, null, ex);
            }
             
            while (true) {
                try {
                    String inputData = in.readLine();
                     out = new PrintStream(socket.getOutputStream(), true);
                     inputData = decode(inputData);
                    request = processRequest(inputData);
                     out.println(encode(request));
                  
                } catch (IOException ex) {
                    Logger.getLogger(ClientSend.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
                 
            }
    }
/*
 * This method takes the data recieved from the server and prcesses it based on the 3 digit code
 * Also this is the method where the user enters data to send to the server
 */
    private String processRequest(String data) {
      
        Scanner input = new Scanner(System.in);
        if (data.length() >= 3) {
            switch (data.substring(0, 3)) {
                case "221":
                    try {
                        System.out.println(data);
                        socket.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ClientSend.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.exit(0);
                    return"";
                case "220":
                       System.out.println(data);
                      return input.nextLine();
                case "330":
                    try {
                        socket.close();
                         System.out.println(data);
                        System.out.print("Restarting ");
                    } catch (IOException ex) {
                        Logger.getLogger(ClientSend.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        for (int i = 5; i >= 1; i--) {
                            Thread.sleep(900); // a little less than a second
                            System.out.print(i + "... ");
                        }
                        String []args = {Driver.HOST, ""+ Driver.PORT};
                        emailclient.Driver.main(args);
                        Thread.currentThread().join();
                        
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientSend.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return "";
                case "334":           
                       System.out.print(data);
                      return input.nextLine();                   
                case "535":
                    System.out.print(data);
                    return input.nextLine();
                default:
                   System.out.println(data.trim());
                    return input.nextLine();
            }
        } else {
            return "";
        }

    }

    private String decode(String data) {
        return new String(Base64.getDecoder().decode(data));

    }

    private String encode(String data) {

        return Base64.getEncoder().encodeToString(data.getBytes());

    }
  
}
