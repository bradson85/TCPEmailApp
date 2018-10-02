/*
 * Bradley Peradotto
 * Last edit 8/26/17
 * 
 * this is the drivere for the client side fo the email server.
 * creates a new thread everytime this program is run
 * it handles the arguments of the correct host and port
 */
package emailclient;

import java.util.Scanner;

public class Driver {

    static String HOST;
    static int PORT;

    public static void main(String[] args) {

        /**
         * *********************************************************************************************
         * FOLOWING IS FOR ENTERING AND ESTABLISHING CORRECT DOMAIN AND PORTS
         *
         **********************************************************************************************
         */
        Scanner input = new Scanner(System.in);

        // series of if else's to validate proper input for ports.
        // to few args
        if (args.length < 1) {
            System.out.println("Error: Too few arguments");
            System.out.println("Sender client needs: <server-hostname> <server-port> as parameter (without \"<>\") ...");
            System.out.println("Client exiting.");
            System.exit(1);
        }
        // to many args
        if (args.length > 2) {
            System.out.println("Error: Too many arguments");
            for (int i = 2; i < 6; i++) {
                System.out.print(args[i] + ", ");

            }
            System.out.println("... is not an argument Client.jar understands. Exiting.");
            System.exit(1);
   /// jsut right amout of args
        } else {
            System.out.println("\nBradley Peradotto's SMTP Client Running...");
            
            try {
                HOST = args[0]; // set host
                PORT = Integer.parseInt(args[1]); //set port
                if (PORT > 0 && PORT <= 65535) {
                    System.out.println("(Type 'quit' to exit SMTP Client.)");
                } else {

                    System.out.println("Number out of range. Valid Ports are between [1-65535]. Exiting");
                    System.exit(1);
                }

            } catch (NumberFormatException e) {
                System.out.print(e);
                System.out.println("Error : argument " + args[1] + " is not an integer. Exiting");
                System.exit(1);
            }

        }
        /**
         * *********************************************************************************************
         * END OF CODE FOR DOMAIN AND PORT SETUP
         *
         **********************************************************************************************
         */

       

        
 /*Start of main TLS Connection Stuff */
        Client client = new Client(PORT, HOST);
        new Thread(client).start();

        
    }
}

