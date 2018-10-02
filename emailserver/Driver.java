/*
 * Bradley Peradotto
 * Last edit 8/26/17
 * 
 * this is the driver for the server side fo the email server.
 * creates a new thread everytime this program is run
 * it handles the arguments of the desired port to run
 */
package emailserver;


import java.util.Scanner;

public class Driver {
    // variables
  private static int PORTTCP;
   
    public static void main(String[] args) {
    
       
          /**
         * *********************************************************************************************
         * FOLOWING IS FOR ENTERING AND ESTABLISHING CORRECT PORTS
         *
         **********************************************************************************************
         */
        Scanner input = new Scanner(System.in);
        String temp = "";
        // series of if else's to validate proper input for ports.
        // to few arguemnts
        if (args.length < 1) {
            System.out.println("Error: incorrect arugments");
            System.out.println("Server needs: <tcp-listen-port> as parameter (without \"<>\") or ...");
            System.out.println("Enter parameter as \"HELP\".");
            System.out.println("EmailServer exiting.");
            System.exit(1);
// correct amout of args
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {// for if help is called
                System.out.println("\nBradley Peradotto's Email Server Running...\nType `help' to see this list.\n"
                        + "Type `help name' to find out more about the function `name'.\n"
                        + "i.e. To find out about 'helo' type 'help helo'");
                System.out.print(helpInfo(args[0]));
                System.out.println("\n");
                System.exit(0);
            }else{ // if not help then checks for a valid port nubmer
                try {
                    PORTTCP = Integer.parseInt(args[0]);
                    if (PORTTCP > 0 && PORTTCP <= 65535) {
                        //do nothing
                    } else {

                        System.out.println("Number out of range. Valid Ports are between [1-65535]. Exiting");
                        System.exit(1);
                    }

                } catch (NumberFormatException e) {
                    System.out.print(e);
                    System.out.println("Error : argument " + args[0] + " is not an integer. Exiting");
                    System.exit(1);
                }

                System.out.println("TCP Port set to:" + PORTTCP);
               
            } 
            // too many args
        } else if (args.length > 2) {
            System.out.println("Error: Too many arguments");
            for (int i = 2; i < 6; i++) {
                System.out.print(args[i] + ", ");

            }
            System.out.println("... is not an arument Emailserver.jar understands. Exiting.");
            System.exit(1);

        } 
                
            
        
        /**
         * *********************************************************************************************
         * END OF CODE FOR PORT SETUP
         *
         **********************************************************************************************
         */
        
       
        
        // establish a thread for to listen for 'quit' to stop program.
        Thread thread2 = new Thread() {
            @Override
            public void run() {
                System.out.println("Type \"quit\" to close server program.");
                Scanner in = new Scanner(System.in);
                String quit = in.nextLine();
                if (quit.equalsIgnoreCase("quit")) {
                    System.exit(0);
                }
            }
        };

        thread2.start();
        /* *******************End of quit thread *****************************************************/
        
        /*Start of main TLS Connection Stuff */
        
        SmtpServer server = new SmtpServer(PORTTCP);
         new Thread(server).start();
        
        /* End of connection Stuff*/
    
    
}
    //Method: helpInfo allows for the if help or help with parameters
    // is entered when the programs runs  i.e  "driver help" or "driver help helo"
    private static String helpInfo(String substring) {

        switch (substring.toUpperCase()) {
            case "HELO":
                return ("Helo: Helo <example@domain.com>\n"
                        + "This greets the server. Must include the email address of sender");
            
            case "AUTH":
                return ("AUTH: AUTH\n"
                        + "This initiates the Authentication sequence with the server.\n"
                        + "After an AUTH command has been successfully completed, no more AUTH commands may be issued");

            case "MAIL FROM":
                return ("MAIL FROM: MAIL FROM <example@domain.com>\n"
                        + "This assigns what address the mail is being sent from. This is the email address of sender");

            case "RCPT TO":
                return ("RCPT TO: RCPT TO <example@domain.com>\n"
                        + "This assigns what address the mail is being sent to. This is the email address of reciever");

            case "DATA":
                return ("DATA: DATA\n"
                        + "This is where user enters email message. While not required"
                        + " should be similar to the following example:\n"
                        + "From:  <tintin@447.edu>\n"
                        + "To:  <haddock@447.edu>\n"
                        + "Subject:  The Last Unicorn\n"
                        + "Dear Haddock,\n"
                        + "Glad to hear that you found the last Unicorn.  We are looking\n"
                        + "forward to your safe return.\n"
                        + "Yours truly,\n"
                        + "Tintin and Snowy.");

            case "QUIT":
                return ("QUIT: QUIT\n"
                        + "This disconnects the client from the server and closes the transmission");

            case "RSET":
                return ("RSET: RSET\n"
                        + " This ends the current mail transaction. Mail data will be discarded. Does not\n"
                        + "disconnect from server and returns to just after HELO in sequence");

            default:
                return ("HELO\nAUTH\nMAIL FROM\nRCPT TO\nDATA\nRSET\nQUIT");

        }
    }
}

