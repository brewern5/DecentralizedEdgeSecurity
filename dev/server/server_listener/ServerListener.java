package server_listener;

import java.io.*;
import java.net.*;

import server_config.ServerConfig;
import server_handler.ServerNodeHandler;

public class ServerListener implements Runnable {
    
    private Socket connected;   // This is the socket sent to the handler after the connection has been made by the listener
    private ServerSocket listenerSocket;    // this is the active listening socket
    private int port;   
    private int timeout;

    public ServerListener(int port, int timeout) throws IOException {
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
                Thread handlerThread = new Thread(
                    new ServerNodeHandler(connected)
                ); // sends the message to a handler
                handlerThread.start(); // Begins the new thread
            } catch (SocketTimeoutException sto) {
                // You need this exception handling here because it will brick at trying to start the connection
                // Did not add any handling to this exception as it would constatly throw an error, so this just prevents it from clogging up the console
            } catch (IOException ioe) {
                System.err.println("IOException!\n");
                ioe.printStackTrace();
            } catch (Exception e) {
                System.err.println("Unknown Exception!\n");
                e.printStackTrace();
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

    public void changePort(int newPort) {
        port = newPort;
        // TODO: Activly change the port in the socket
    }

    public void changeTimeout(int newTimeout) {
        timeout = newTimeout;
        try {
            listenerSocket.setSoTimeout(timeout);
        } catch (Exception e) {
            System.err.println("Error setting new timeout on socket: ( " + port + " )");
        }
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
