/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package keygen;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Scanner;


public class Driver {

    public static void main(String[] args) {
        ExecuteShellCommand com = new ExecuteShellCommand();
        Scanner in = new Scanner(System.in);
        boolean correctInput = false;
        System.out.println("Welcome to Brad's keystore and certificate program.\n\n "
                + "Would you like to make the keys and certificates for the client or the server?");

        while (!correctInput) {
            System.out.print("Client or Server?: ");
            String result = in.nextLine();
            if (result.equalsIgnoreCase("server")) {
                 checkIfExists("emailserver");
                createDBFileFolder("emailserver");
                com.executeCommand("keytool -genkey -noprompt -keyalg RSA -alias 447.edu "
                        + "-dname CN=447.edu,OU=447.edu,O=CS447,L=Edwardsville,S=IL,C=US"
                        + " -keystore emailServer/db/keystore.jks -storepass waz8nob9 -keypass waz8nob9");

                com.executeCommand("keytool -export -alias 447.edu "
                        + "-storepass waz8nob9 -file emailServer/db/server.cer -keystore EmailServer/db/keystore.jks");

                // this needs copied to trust store in client.
                com.executeCommand("keytool -import -v -trustcacerts -noprompt "
                        + "-alias 447.edu -file emailserver/db/server.cer -keystore emailserver/db/client_truststore.certs"
                        + " -keypass waz8nob9 -storepass waz8nob9");

                System.out.println("\n REMEMBER TO MOVE THE CLIENT TRUSTSTORE FILE GENERATED FROM THE SERVER OVER TO THE CLIENT TRUSTSTORE");
                correctInput = true;
            } else if (result.equalsIgnoreCase("client")) {
                 checkIfExists("emailclient");
                createDBFileFolder("emailclient");
                com.executeCommand("keytool -genkey -noprompt -keyalg RSA -alias 447-client "
                        + "-dname CN=447.edu,OU=447.edu,O=CS447,L=Edwardsville,S=IL,C=US"
                        + " -keystore emailClient/db/keystore.jks -storepass waz8nob9 -keypass waz8nob9");

                com.executeCommand("keytool -export -alias 447-client "
                        + "-storepass waz8nob9 -file emailclient/db/client.cer -keystore EmailClient/db/keystore.jks ");

                com.executeCommand("keytool -import -v -trustcacerts -noprompt "
                        + "-alias 447-client -file emailclient/db/client.cer -keystore emailclient/db/server_truststore.certs"
                        + " -keypass waz8nob9 -storepass waz8nob9");

                System.out.println("\n REMEMBER TO MOVE THE SERVER TRUSTSTORE FILE GENERATED FROM THE CLIENT OVER TO THE SERVER TRUSTSTORE");
                correctInput = true;
            } else {
                System.out.println("Incorrect input.");
            }

        }
    }

    private static void createDBFileFolder(String which) {
        File directory = new File(which);
        if (!directory.exists()) {
            directory.mkdir();
        }

        directory = new File(which + "/db");
        if (!directory.exists()) {
            directory.mkdir();
        }

    }

    private static void checkIfExists(String which) {
       File directory = new File(which + "/db");
       if(directory.exists()){
       String[]entries = directory.list();
     for(String s: entries){
    File currentFile = new File(directory.getPath()+"/"+s);
   System.out.println(currentFile.getPath()); 
     }
       }
}
}
class ExecuteShellCommand {

    public String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }

}
