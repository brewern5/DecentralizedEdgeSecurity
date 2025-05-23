package listeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

import config.NodeConfig;
import handler.NodeClientHandler;

public class Listener implements Runnable {
    
    private static Socket connected;

    private static ServerSocket listenerSocket;
    private static int port;

    public Listener(int port, int timeout) throws IOException {
        this.listenerSocket = new ServerSocket(port);
        this.port = port;

        listenerSocket.setSoTimeout(timeout);
        System.out.println("Listening on port " + port);
    }

    @Override
    public void run(){  

        boolean on = true;

        while(on){
            try {
                connected = listenerSocket.accept();
                Thread handlerThread = new Thread(new NodeClientHandler(connected)); // sends the message to a handler
                handlerThread.start(); // Begins the new thread
            } catch (SocketTimeoutException sto) {
                System.err.println("  1  ");
                sto.printStackTrace();

            } catch (IOException ioe) {
                System.err.println("IOException!\n");
                ioe.printStackTrace();
            }
        }


    }

    //          Accessor methods
    public int getActivePort() {
        return port;
    }

    public boolean closeSocket() {
        try{
            listenerSocket.close();
        } catch(Exception e) {
            System.err.println("Error Closing socket on port: ( " + port + " )");
            return false;
        }
        return true;
        
    }
}
