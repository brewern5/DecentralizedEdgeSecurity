package listeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

import config.ServerConfig;
import handler.ServerClientHandler;

public class Listener implements Runnable {
    
    private Socket connected;
    private ServerSocket listenerSocket;
    private int port;
    private int timeout;

    public Listener(int port, int timeout) throws IOException {
        this.port = port;
        this.timeout = timeout;
        this.listenerSocket = new ServerSocket(port);
        this.listenerSocket.setSoTimeout(timeout);
    }

    @Override
    public void run(){  

        System.out.println("Listening on port " + this.port);
        System.out.flush(); // clears system.out to prevent overflow

        boolean on = true;
        while(on){
            try {
                connected = listenerSocket.accept();
                Thread handlerThread = new Thread(new ServerClientHandler(connected)); // sends the message to a handler
                handlerThread.start(); // Begins the new thread
            } catch (SocketTimeoutException sto) {
                // You need this exception handling here because it will brick at trying to start the connection
                // Did not add any handling to this exception as it would constatly throw an error, so this just prevents it from clogging up the console
            } catch (IOException ioe) {
                System.err.println("IOException!\n");
                ioe.printStackTrace();
            }
        }
    }

    /*          Accessor methods        */
    public int getActivePort() {
        return port;
    }

    public int getActiveTimeout() {
        return timeout;
    }


    /*          Changer methods        */
    public void changeTimeout(int newTimeout) {
        timeout = newTimeout;
    }

    public boolean closeSocket() {
        try{
            System.out.println("Closing socket on port: + ( " + port + " ) ");
            listenerSocket.close();
        } catch(Exception e) {
            System.err.println("Error Closing socket on port: ( " + port + " )");
            return false;
        }
        return true;
    }
}
