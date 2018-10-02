/*
 * Bradley Peradotto
 * Last Edit 6/26/17
 *
 * This is the code for managing the incoming messages from the client. 
 * Processes data, authenticates users, and saves completed emails.
 * This responnds with appropriate codes as well to the client.
 */
package emailserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLSocket;

public class SmtpResponse implements Runnable {
// constants

    private final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@(?i)447.edu$", Pattern.CASE_INSENSITIVE);

    //variables
    private String currCommand;
    private Socket socket;
    private String data;
    private ArrayList<MetaData> state;
    private int passAttempts;
    private String currSender;
    private int currState;
    private String currRec;
    private int fileNumber;

    // constructor
    public SmtpResponse(SSLSocket socket) {
        this.socket = socket;
        this.state = new ArrayList<>();
        this.currCommand = "";

    }
// main method for handling message data

    @Override
    public void run() {
        try {
            state.add(new MetaData(0, 0, InetAddress.getLocalHost(), 0, " ", " ", 0)); // add empty object to initialize state data structure
        } catch (UnknownHostException ex) {
            Logger.getLogger(SmtpResponse.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream out = new PrintStream(socket.getOutputStream(), true);

            while (true) {
                data = processMessages(in.readLine()); // read incoming message
                if (data.equalsIgnoreCase("000")) { // special code if close happens
                    out.println(encode("221 447.edu closing connection"));
                    break;
                } else {
                    processLogs(currCommand, decode(data));// manage data
                    out.println(data); // send data back to client
                }
            }

        } catch (IOException e) {
            System.err.println(e);
        }

    }

    // This meathod takes incoming data processes it and gives appropriate response
    private String processMessages(String data) {
        String decodedData = decode(data);  // decode base 64 data
        MetaData currentUser = configStateUser(); // establishes what sequence oreder each clinet is int
        switch (decodedData) {
            case "help":  // if help is the message
                currCommand = "SMTP-HELP";
                return encode(helpInfo(decodedData));
            case "rset": // if reset is the incomming message
                currState = 0;
                currCommand = "SMTP-RSET";
                processStateInfo(currentUser, false);
                return encode("250 OK");
            case "quit": // if quit is the incoming message
                processLogs("SMTP-Quit", "221 Close Connection");
                processStateInfo(currentUser, true);
                return "000"; // special code that get intercepted by server before it can send to the client;
            default:
                String temp = "";
                switch (getCurrentState(currentUser)) {
                    case 0:  // NEW CONNECION
                        currState = 1;
                        currCommand = "SMTP-NEW CONNECTION";
                        processStateInfo(currentUser, false);
                        return encode("220 447.edu SMTP Services Ready.");
                    case 1: // HELO
                        currCommand = "SMTP-HELO";
                        temp = hello(decodedData);
                        processStateInfo(currentUser, false);
                        return encode(temp);
                    case 2: //AUTH
                        currCommand = "SMTP-AUTH";
                        temp = authenticate(decodedData);
                        processStateInfo(currentUser, false);
                        return encode(temp);
                    case 3: // ENTER USERNAME (ENCRYPTED)
                        currCommand = "SMTP-USERNAME";
                        temp = user(decodedData);
                        if (temp.substring(0, 3).equalsIgnoreCase("330")) {
                            processStateInfo(currentUser, true);
                        } else {
                            processStateInfo(currentUser, false);
                        }
                        return encode(temp);
                    case 4: // PASSWORD (ENCRYPTED)
                        currCommand = "SMTP-PASSWORD";
                        temp = password(decodedData);
                        processStateInfo(currentUser, false);
                        return encode(temp);
                    case 5: //MAIL FROM
                        currCommand = "SMTP-MAIL FROM";
                        temp = mailFrom(decodedData);
                        processStateInfo(currentUser, false);
                        return encode(temp);
                    case 6: // RCPT TO
                        currCommand = "SMTP-RCPT TO";
                        temp = rcptTo(decodedData);
                        processStateInfo(currentUser, false);
                        return encode(temp);
                    case 7: // DATA (START MAIL)
                        currCommand = "SMTP-DATA";
                        temp = startEmail(decodedData);
                        processStateInfo(currentUser, false);
                        return encode(temp);
                    case 8: // FINISH EMAIL
                        currCommand = "SMTP-WRITE EMAIL";
                        temp = buildEmail(decodedData);
                        processStateInfo(currentUser, false);
                        return encode(temp);
                    default:
                        return "500 Default: Unrecognized command";
                }
        }
    }
// simple getter for current state.

    private int getCurrentState(MetaData currentUser) {

        return currentUser.getState();

    }
// this method creates a new entry in the Array for new connections
// or loads the current order int the mail sequence for recurring connections

    private MetaData configStateUser() {
        MetaData t = new MetaData();
        boolean isNew = false;
        String a = socket.getInetAddress().getHostAddress();
        String b;
        for (int i = 0; i < state.size(); i++) {
            b = state.get(i).getAddress().getHostAddress();
            if (a.equals(b) && t.getPort() == state.get(i).getPort()) {
                t.setAddress(state.get(i).getAddress());
                t.setPort(state.get(i).getPort());
                t.setState(state.get(i).getState());
                t.setReciever(state.get(i).getReciever());
                t.setSender(state.get(i).getSender());
                t.setFilenumber(state.get(i).getFilenumber());
                t.setPassAttempts(state.get(i).getPassAttempts());
                isNew = false;
                break;
            } else {
                t.setAddress(socket.getInetAddress());
                t.setPort(socket.getPort());
                t.setState(0);
                t.setPassAttempts(0);
                isNew = true;
            }

        }
        if (isNew) {
            state.add(t); // addd to array
        } else {

        }
        return t;
    }

    // this is a handler for save and deleteing teh current sequence data
    // if quit flag is true, removes datat complely from server
    private void processStateInfo(MetaData a, boolean quitFlag) {
        if (quitFlag) {
            deleteCurrentStateInfo(a);
        } else {
            deleteCurrentStateInfo(a);
            saveCurrentStateInfo(a);
        }
    }

    // saves the current state to the array
    private void saveCurrentStateInfo(MetaData a) {
        a.setState(currState);
        a.setReciever(currRec);
        a.setSender(currSender);
        a.setFilenumber(fileNumber);
        a.setPassAttempts(passAttempts);
        state.add(a);
    }

    // deletes teh datat from the array
    private void deleteCurrentStateInfo(MetaData t) {
        String a = t.getAddress().getHostAddress();
        String b;
        for (int i = 0; i < state.size(); i++) {
            b = state.get(i).getAddress().getHostAddress();
            if (a.equals(b) && t.getPort() == state.get(i).getPort()) {
                state.remove(i);
            }
        }

    }

// handler for hello messages if data is incorrect or out of order responseds with appropriate error codes
    private String hello(String data) {
        if (data.length() >= 4) {
            if (data.substring(0, 4).compareToIgnoreCase("helo") == 0) {
                if (!(data.length() < 5)) {
                    if (checkValidDomain(data.substring(5))) {
                        currState = 2;
                        return "250 447.edu says hello";
                    } else {
                        return "501 Include your domain name \"user@447.edu\"";
                    }
                } else {
                    return "501 Include your domain name \"user@447.edu\"";
                }
            } else if (checkCommands(data)) {
                if (checkHelp(data)) {
                    return helpInfo(data);
                } else {
                    return "503 Polite people say Helo first";
                }

            } else {
                return "503 Polite people say Helo first";
            }
        } else {

            return "503 Polite people say Helo first";
        }
    }
// handler for Auth message. if data is incorrect or out of order responds with appropriate error codes

    private String authenticate(String data) {
        if (data.length() >= 4) {
            if (data.substring(0, 4).compareToIgnoreCase("auth") == 0) {
                currState = 3;
                return "334 " + encode("username");
            } else if (checkCommands(data)) {
                if (checkHelp(data)) {
                    return helpInfo(data);
                } else {
                    return "530 Please Authenticate credentials with \"AUTH\".\"";
                }

            } else {
                return "500 Syntax error, command unrecognized";
            }
        } else {

            return "500 Syntax error, command unrecognized";
        }
    }

    // handler for Username data. if data is incorrect or out of order responds with appropriate error codes
    private String user(String data) {
        data = data.split("\\@")[0];
        if (checkCommands(data)) {
            if (checkHelp(data)) {
                return helpInfo(data);
            } else {
                return "530 Incorrect command. Please enter your username.";
            }

        } else if (data.length() > 2) {
            if (authenticateUser(data)) { // if current user
                currState = 4;
                currSender = data;
                return "334 " + encode("password");
            } else {  // if new user
                String tPass = RandomPasswordGenerator();
                String sPass = encodePassword(tPass);
                writeAuthFile(data, sPass);
                return "330 New username stored. Here is your password: " + tPass;
            }
        } else {
            return "Please enter a username with at least 3 characters";
        }

    }

// handler for passwords input. if data is incorrect or out of order responds with appropriate error codes
    // if there are too many password attempts lock out user
    private String password(String data) {
        if (checkCommands(data)) {
            if (checkHelp(data)) {
                return helpInfo(data);
            } else {
                return "530 Incorrect command. Please enter your password.";
            }
        } else if (authenticateUserPassword(currSender + "&" + encodePassword(data))) { // if successful

            passAttempts = 0;
            currState = 5;
            return "235 Login Successful";
        } else { // if password fails
            passAttempts++;
            if (passAttempts >= 5) {
                return "221  Too many failed password attempts.\n 447.edu closing connection";
            } else {
                return "535 invalid password. Please renter your password.\nPassword:";
            }
        }

    }

    // handler for "mail from" in sequence. if data is incorrect or out of order responds with appropriate error codes
    private String mailFrom(String data) {
        if (data.length() >= 10) {
            if (data.substring(0, 9).compareToIgnoreCase("mail from") == 0) {
                if (checkValidDomain(data.substring(10))) {
                    currSender = data.substring(10);
                    currState = 6;
                    return "250 <" + data.substring(10) + ">.... OK";
                } else {
                    return "501 Include the domain name \"example@447.edu\"";
                }
            } else {
                return "503 Choose Sender's Domain by using command: \"MAIL FROM\".";
            }
        } else {
            if (data.compareToIgnoreCase("auth") == 0) {
                return "503  Already Authenticated. Choose Sender's Domain by using command: \"MAIL FROM\".";
            } else {
                return "503 Choose Sender's Domain by using command: \"MAIL FROM\".";
            }
        }

    }

    // handler for "rcpt to". if data is incorrect or out of order responds with appropriate error codes
    private String rcptTo(String data) {
        if (data.length() >= 8) {
            if (data.substring(0, 7).compareToIgnoreCase("RCPT TO") == 0) { // 6 because thats the length of Mail from
                if (checkValidDomain(data.substring(8))) {

                    currState = 7;
                    currRec = data.substring(8).split("\\@")[0];
                    createIndiFileFolder(currRec);
                    fileNumber = createNewFile(currRec, currSender, fileNumber);
                    return "250 <" + data.substring(8) + ">... OK";
                } else {
                    return "501 Include the domain name \"example@447.edu\"";
                }
            } else {
                return "503 Choose Reciever's Domain by using command: \"RCPT TO\".";
            }
        } else {
            return "503 Choose Reciever's Domain by using command: \"RCPT TO\".";
        }
    }

    // handler for "DATA". if data is incorrect or out of order responds with appropriate error codes
    private String startEmail(String data) {
        if (data.length() >= 4) {
            if (data.substring(0, 4).compareToIgnoreCase("Data") == 0) {
                currState = 8;
                return "354 Enter Message,  end with \".\" on a single line";
            } else {
                return "503 Enter Message by using command: \"DATA\".";
            }
        } else {
            return "503 Enter Message by using command: \"DATA\".";
        }
    }

    // handler for buildig email.
// if data is incorrect or out of order responds with appropriate error codes
    // waits for the single . that signifies end of messages.
    private String buildEmail(String data) {
        if (data.endsWith(".")) {
            currState = 5;
            return "250 Message Recieved";
        } else {
            appendFile(currRec, data, fileNumber);
            return "    "; // return more than three spaces becuause client looks for at least 3 spaces
        }
    }

    // makes sure @447 domain is used
    private boolean checkValidDomain(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }

    // decodes base64 messages
    private String decode(String data) {
        String decode = "";
        try {
            decode = new String(Base64.getDecoder().decode(data));
        } catch (NullPointerException e) {
            
        }
        return decode;

    }
// encodes messages to base 64
    private String encode(String data) {

        return Base64.getEncoder().encodeToString(data.getBytes());

    }

    // sees if common commands are used. fro when out of sequence
    private boolean checkCommands(String data) {
        return (data.equalsIgnoreCase("helo")) || (data.equalsIgnoreCase("mail from"))
                || (data.equalsIgnoreCase("help")) || (data.equalsIgnoreCase("rcpt to")) || (data.equalsIgnoreCase("data"));
    }

    // checks to see if help command is used
    private boolean checkHelp(String data) {

        return (data.equalsIgnoreCase("help"));
    }

    // returns help messages 
    private String helpInfo(String string) {
        if (string.length() > 5) {
            String substring = string.substring(5);
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
                            + "disconnect from server and returns to just before HELO in sequence");

                default:
                    return ("help [option] \n List of options: \nHELO\nAUTH\nMAIL FROM\nRCPT TO\nDATA\nRSET\nQUIT");

            }
        } else {
            return ("help [option] \n List of options: \nHELO\nAUTH\nMAIL FROM\nRCPT TO\nDATA\nRSET\nQUIT");

        }
    }

    // creates new email files
    private int createNewFile(String fileName, String data, int fileNumber) {
        int filenumber = fileNumber;

        File file = new File("emailserver/db/" + fileName + "/" + filenumber + ".email");

        while (file.exists()) {
            filenumber++;
            file = new File("emailserver/db/" + fileName + "/" + filenumber + ".email");
        }

        try {
            FileWriter fwriter = new FileWriter(file);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write("Date: ");
            bwriter.write(String.valueOf(new Date().toString() + "\n"));
            bwriter.write("From: <" + data + ">\nTo: <" + fileName + "@447.edu> \n");
            bwriter.close();
            fwriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return filenumber;
    }

    // for making teh appropriate directories if they don;t exist
    private void createDBFileFolder() {
        File directory = new File("emailserver");
        if (!directory.exists()) {
            directory.mkdir();
        }
        directory = new File("emailserver/db");
        if (!directory.exists()) {
            directory.mkdir();
        }

    }

   // for making  appropriate directore and email file if they dont exist
    private void createIndiFileFolder(String name) {
        File directory = new File("emailserver");
        if (!directory.exists()) {
            directory.mkdir();
        }
        directory = new File("emailserver/db");
        if (!directory.exists()) {
            directory.mkdir();
        }
        directory = new File("emailserver/db/" + name + "/");
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    // method for adding data to approprate email file
    private void appendFile(String filename, String data, int filenumber) {
        File file = new File("emailserver/db/" + filename + "/" + filenumber + ".email");
        //System.out.println(file);
        try {
            FileWriter fwriter = new FileWriter(file, true);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(data + " \n");
            bwriter.close();
            fwriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // a random 5 digit password generator
    private String RandomPasswordGenerator() {
        Random rand = new Random();
        int number = rand.nextInt(100000);
        String formatted = String.format("%05d", number);
        return (formatted);

    }

    // encodes passwords with appropriate encoding.
    // number + 447 then base 64 encode
    private String encodePassword(String password) {

        int tempPass = 0;
        try {
            tempPass = Integer.parseInt(password);
        } catch (NumberFormatException ex) {
            System.err.println(ex);
        }
        tempPass = tempPass + 447;
        password = "" + tempPass;
        return (Base64.getEncoder().encodeToString(password.getBytes()));
    }

    // writes to a new hidden file .user_pass
    private void writeAuthFile(String username, String password) {

        createDBFileFolder();

        String Os = System.getProperty("os.name");
        if (Os.equalsIgnoreCase("windows")) {
            try {
                File file = new File("emailserver/db/user_pass");

                FileWriter fwriter = new FileWriter(file, true);

                BufferedWriter bwriter = new BufferedWriter(fwriter);
                bwriter.write(username + "&" + password + "\n");
                bwriter.close();
                fwriter.close();

                Runtime.getRuntime().exec("attrib +H db/user_pass");

            } catch (IOException ex) {
                Logger.getLogger(SmtpServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            try {
                File file = new File("emailserver/db/.user_pass");
                FileWriter fwriter = new FileWriter(file, true);

                BufferedWriter bwriter = new BufferedWriter(fwriter);
                bwriter.write(username + "&" + password + "\n");
                bwriter.close();
                fwriter.close();

            } catch (IOException ex) {
                Logger.getLogger(SmtpServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
// checks .user_pass file to see if user exists
    private boolean authenticateUser(String data) {
        createDBFileFolder();
        String temp;
        BufferedReader bReader = null;
        FileReader fReader = null;
        String Os = System.getProperty("os.name");
        if (Os.equalsIgnoreCase("windows")) {
            try {
                Runtime.getRuntime().exec("attrib -H db/user_pass");
                File file = new File("emailserver/db/user_pass");
                fReader = new FileReader(file);
                bReader = new BufferedReader(fReader);

                while ((temp = bReader.readLine()) != null) {
                    if (temp.split("\\&")[0].equals(data)) {
                        Runtime.getRuntime().exec("attrib +H db/user_pass");
                        return true;
                    }
                }

            } catch (IOException e) {

                System.err.println(e);

            }

        } else {

            try {
                File file = new File("emailserver/db/.user_pass");
                fReader = new FileReader(file);
                bReader = new BufferedReader(fReader);

                while ((temp = bReader.readLine()) != null) {
                    if (temp.split("\\&")[0].equals(data)) {
                        return true;
                    }
                }

            } catch (IOException e) {

                System.err.println(e);

            }

        }
        return false;
    }

    // check to .user_pass file to see if user password combination exists
    private boolean authenticateUserPassword(String data) {
        createDBFileFolder();
        String temp;
        BufferedReader bReader = null;
        FileReader fReader = null;
        String Os = System.getProperty("os.name");
        if (Os.equalsIgnoreCase("windows")) {
            try {
                Runtime.getRuntime().exec("attrib -H db/user_pass");
                File file = new File("emailserver/db/user_pass");
                fReader = new FileReader(file);
                bReader = new BufferedReader(fReader);

                while ((temp = bReader.readLine()) != null) {
                    if (temp.equals(data)) {
                        Runtime.getRuntime().exec("attrib +H db/user_pass");
                        return true;
                    }
                }

            } catch (IOException e) {

                System.err.println(e);

            }

        } else {

            try {
                File file = new File("emailserver/db/.user_pass");
                fReader = new FileReader(file);
                bReader = new BufferedReader(fReader);

                while ((temp = bReader.readLine()) != null) {
                    if (temp.equals(data)) {
                        return true;
                    }
                }

            } catch (IOException e) {

                System.err.println(e);

            }

        }
        return false;

    }

    // method for logging data
    private void processLogs(String protocol, String message) {
        ServerLogger logger = new ServerLogger(Logger.class.getName(), null);
        logger.customLog(socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort(), socket.getInetAddress().getHostAddress(), protocol, message);

    }

}
