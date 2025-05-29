package listeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

import config.CoordinatorConfig;
import handler.CoordinatorServerHandler;

public class CoordinatorListener implements Runnable {
    
    private Socket connected;
    private ServerSocket listenerSocket;
    private int port;
    private int timeout;

    public CoordinatorListener(int port, int timeout) throws IOException {
        this.port = port;
        this.timeout = timeout;
        this.listenerSocket = new ServerSocket(port);
        this.listenerSocket.setSoTimeout(timeout);
    }

    @Override
    public void run(){  

        System.out.println("Listening on port " + this.port);
        System.out.flush();

        boolean on = true;
        while(on){
            try {
                connected = listenerSocket.accept();
                Thread handlerThread = new Thread(new CoordinatorServerHandler(connected)); // sends the message to a handler
                handlerThread.start(); // Begins the new thread
            } catch (SocketTimeoutException sto) {
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
