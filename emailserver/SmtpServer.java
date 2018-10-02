/*
 * Bradley Peradotto
 * Last Edit 6/26/17
 *
 * This is the code the connections for incoming clients
 * It loads teh keystores and truststores for the server
 * establishes the TLS handshake with clients
 */
package emailserver;

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
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class SmtpServer implements Runnable {
    // constants 
    // this is a list of common safe ciphers
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
    protected Thread runningThread;
    KeyStore key;
    KeyStore trust;

    // construcotor
    public SmtpServer(int port) {
        this.port = port;
        this.runningThread = null;
        this.key = null;
        this.trust = null;
    }
// main method for running connections managaer
    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
      
        //keystore stuff
        String password = "waz8nob9";
        File keyFile = new File("emailserver/db/keystore.jks");
        if(!keyFile.exists()){
         System.err.println("No such file " + keyFile+"\n Please create keystore.");
         System.exit(1);
        }
        KeyManagerFactory keyManager = null; 
        keyManager = loadKeyStore(password, keyFile);

        // Truststore Stuff
        File trustFile = new File("emailserver/db/server_truststore.certs");
          if(!trustFile.exists()){
         System.err.println("No such file " + trustFile+"\n Please create truststore.");
         System.exit(1);
        }
        TrustManagerFactory trustManager = null;
        trustManager = loadTrustStore(password, trustFile);
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManager.getKeyManagers(), trustManager.getTrustManagers(), null);
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            Logger.getLogger(SmtpServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        SSLServerSocketFactory fSocket = sslContext.getServerSocketFactory();
        SSLServerSocket sSocket = null;
        SSLSocket lSocket = null;
        try {
            sSocket = (SSLServerSocket) fSocket.createServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(SmtpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        String []ciphers;
        while (true) {
            try {
               
                sSocket.setUseClientMode(false);
                sSocket.setEnabledProtocols(new String[] {"TLSv1", "TLSv1.1", "TLSv1.2"}); // tls only
              ciphers = intersection(sSocket.getSupportedCipherSuites(),cipherList);
                 sSocket.setEnabledCipherSuites(ciphers);
                 sSocket.setNeedClientAuth(true); // client must also authenticate
              lSocket = (SSLSocket) sSocket.accept(); // wiat for connection
                new Thread( new SmtpResponse(lSocket)).start(); // start new message handler
            } catch (IOException ex) {
                Logger.getLogger(SmtpServer.class.getName()).log(Level.SEVERE, null, ex);
            }  
        }
    }

    // loads the appropirate keyStore connection and returns namger reference
    // to use later for the SSL context

    private KeyManagerFactory loadKeyStore(String password, File file) {
        KeyManagerFactory keyManager = null;
        try {
            InputStream keyStoreResource = new FileInputStream(file);
            this.key = KeyStore.getInstance("JKS");
            this.key.load(keyStoreResource, password.toCharArray());
            keyManager = KeyManagerFactory.getInstance("SunX509");
            keyManager.init(this.key, password.toCharArray());

        } catch (NoSuchAlgorithmException | FileNotFoundException | KeyStoreException | UnrecoverableKeyException ex) {
            System.err.println(ex);

        } catch (IOException | CertificateException ex) {
            Logger.getLogger(SmtpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
        
        return keyManager;
    }


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
            Logger.getLogger(SmtpServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException ex) {
            Logger.getLogger(SmtpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
             
           
        

        return trustManager;
    }   

   
    // for matching ciphre list to supporte JSSE ciphers
public String[] intersection(String[] stringSetA, String[] stringSetB) {
        Set<String> intersection = new HashSet<>(Arrays.asList(stringSetA));
        intersection.retainAll(Arrays.asList(stringSetB));
        return intersection.toArray(new String[intersection.size()]);
    }
}
