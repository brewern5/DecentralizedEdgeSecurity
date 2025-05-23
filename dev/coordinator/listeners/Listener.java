package listeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

import config.CoordConfig;
import handler.CoordClientHandler;

public class Listener implements Runnable {
    
    private Socket connected;
    private ServerSocket listenerSocket;
    private int port;

    public Listener(int port, int timeout) throws IOException {
        this.port = port;
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
                Thread handlerThread = new Thread(new CoordClientHandler(connected)); // sends the message to a handler
                handlerThread.start(); // Begins the new thread
            } catch (SocketTimeoutException sto) {
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
