/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emailserver;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


public class ServerLogger extends Logger {
    
    private final String file = "emailserver/db/.server_log";
    
    
    public ServerLogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }
    
    public void customLog(String toIP, String fromIP, String protocolCmd,String message){
        Logger logger = Logger.getLogger(ServerLogger.class.getName());
        logger.setUseParentHandlers(false);
        
        
        ServerLogFormat formatter = new ServerLogFormat(toIP,fromIP, protocolCmd);
        
        FileHandler handler;
        try {
            handler = new FileHandler(file, true);
            handler.setFormatter(formatter);
            logger.addHandler(handler);
            logger.info(message);
            handler.close();
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(ServerLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
           
    
    }
    
}


// sublcass formatter to handel logs
class ServerLogFormat extends Formatter{
    
     private static final DateFormat DATE = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
     private final String toIP;
     private final String fromIP;
     private final String protocolCmd;

    public ServerLogFormat(String toIP, String fromIP, String protocolCmd) {
        this.toIP = toIP;
        this.fromIP = fromIP;
        this.protocolCmd = protocolCmd;
       
    }
    
    
    
     @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);
        builder.append(DATE.format(new Date(record.getMillis()))).append(" - ");
        builder.append("").append(toIP).append(" | ");
        builder.append("").append(fromIP).append(" | ");
        builder.append("").append(protocolCmd).append(" | ");
        builder.append(formatMessage(record));
        builder.append("\n");
        return builder.toString();
    }
    
}


