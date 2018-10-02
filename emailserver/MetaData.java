/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emailserver;

import java.net.InetAddress;


public class MetaData  {

    private int port;
    private int state;
    private InetAddress address;
    private int filenumber;
    private String reciever;
    private String sender;
    private int passAttempts;

    public MetaData() {
        
    }
    
    public MetaData(MetaData copy){
     this.address = copy.address;
     this.filenumber = copy.filenumber;
     this.passAttempts = copy.passAttempts;
     this.port = copy.port;
     this.reciever = copy.reciever;
     this.sender = copy.sender;
     this.state = copy.state;
    
    }

    public MetaData(int port, int state, InetAddress address, int filenumber, String reciever, String sender, int passAttempts) {
        this.port = port;
        this.state = state;
        this.address = address;
        this.filenumber = filenumber;
        this.reciever = reciever;
        this.sender = sender;
        this.passAttempts= passAttempts;
    }
    
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getFilenumber() {
        return filenumber;
    }

    public void setFilenumber(int filenumber) {
        this.filenumber = filenumber;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public int getPassAttempts() {
        return passAttempts;
    }

    public void setPassAttempts(int passAttempts) {
        this.passAttempts = passAttempts;
    }
    
    


}
