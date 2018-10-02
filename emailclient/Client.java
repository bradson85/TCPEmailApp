/*
 * Bradley Peradotto 
 * Last edit 6/26/17
 *
 * This is the client handler class calle "Client" for the CS 447 email project 3.
 * It loads the clients keystores and trustsotres and establishes the TLS connection with the server.
 */
package emailclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class Client implements Runnable {
    // A list of safe encryption standars for use to campare with what JSSE uses.
    final String []cipherList =  {"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
"TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
"TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
"TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
"TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
"TLS_DHE_RSA_WITH_AES_256_CBC_SHA256"};
 
    // variables
    int port;
    String host;
    protected Thread runningThread;
    KeyStore key;
    KeyStore trust;

    // constructor
    public Client(int port,String host) {
        this.port = port;
        this.host = host;
        this.runningThread = null;
        this.key = null;
        this.trust = null;
    }
   // were all the running code takes place
    @Override
    public void run() {
         synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
      
        //keystore stuff
        String password = "waz8nob9";
        File keyFile = new File("emailclient/db/keystore.jks");
          if(!keyFile.exists()){
         System.out.println("No such file " + keyFile +"\n Please create keystore.");
         System.exit(1);
        }
        String []ciphers; // create cipher string
        
        
        KeyManagerFactory keyManager = null;
        keyManager = loadKeyStore(password, keyFile);

        // Truststore Stuff
        File trustFile = new File("emailclient/db/client_truststore.certs");
          if(!trustFile.exists()){
         System.out.println("No such file " + trustFile+"\n Please create truststore.");
         System.exit(1);
        }
        TrustManagerFactory trustManager = null;
        trustManager = loadTrustStore(password, trustFile);
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");  // create for tls
            // add keys and truststores to current context
            sslContext.init(keyManager.getKeyManagers(), trustManager.getTrustManagers(), null);
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        SSLSocketFactory fSocket = sslContext.getSocketFactory();  // set up ssl socket
        SSLSocket lSocket = null;
       
        try {
            lSocket = (SSLSocket) fSocket.createSocket(host, port); // cast to ssl socket
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
          lSocket.setUseClientMode(true);
          lSocket.setEnabledProtocols(new String[] {"TLSv1", "TLSv1.1", "TLSv1.2"}); // want to use tls
          ciphers = intersection(lSocket.getSupportedCipherSuites(),cipherList); // compare list above to sockets supported list
          lSocket.setEnabledCipherSuites(ciphers); // enable ciphers
            new Thread(new ClientSend(lSocket)).start(); // message handling thread.
        
    }

    // creates all the appropirate keyStore connection and returns namger reference
    // to use later for the SSL context

    private KeyManagerFactory loadKeyStore(String password, File file) {
        KeyManagerFactory keyManager = null;
        try {
            InputStream keyStoreResource = new FileInputStream(file);
            this.key = KeyStore.getInstance("JKS");  // generica java keys
            this.key.load(keyStoreResource, password.toCharArray());
            keyManager = KeyManagerFactory.getInstance("SunX509"); // for certificates
            keyManager.init(this.key, password.toCharArray());

        } catch (NoSuchAlgorithmException | FileNotFoundException | KeyStoreException | UnrecoverableKeyException ex) {
            System.err.println(ex);

        } catch (IOException | CertificateException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
        
        return keyManager;
    }

    
// this is for loading which servers client trusts
    // certificate is saved in local files
    private TrustManagerFactory loadTrustStore(String password, File file) {

        InputStream trustStoreFile;
        TrustManagerFactory trustManager = null;
        try {
            trustStoreFile = new FileInputStream(file);
            this.trust = KeyStore.getInstance("JKS");
            this.trust.load(trustStoreFile, password.toCharArray());
            trustManager = TrustManagerFactory.getInstance("SunX509");
            trustManager.init(this.trust);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
             
           
        

        return trustManager;
    } 

    
    // code for seeing if cipherList used is available in
    public String[] intersection(String[] stringSetA, String[] stringSetB) {
        Set<String> intersection = new HashSet<>(Arrays.asList(stringSetA));
        intersection.retainAll(Arrays.asList(stringSetB));
        return intersection.toArray(new String[intersection.size()]);
    }

}
