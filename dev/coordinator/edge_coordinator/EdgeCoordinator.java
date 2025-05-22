package edge_coordinator;

import java.io.*;
import java.net.*;
import java.rmi.ConnectIOException;
import java.util.*;

import config.Config;       // The configuration file for the entire network
import handler.CoordClientHandler;

public class EdgeCoordinator {

    private static String IP;
    private static int port;

    private static ServerSocket listening;      // The socket that will be listening to requests from the Edge server.
    private static Socket sending;        // The socket that will send messages to edge server

    private static Socket clientSocket;

    /*
     *  Initalizes the Node Coordinator - This will (probably) be the first thing that runs when the Node Coordinator is started up
     * 
     */

    public static void init() {

        Config config = new Config();

        // try/catch to generate the IP from ./Config.java - Throws UnknownHostException if it cannot determine the IP
        try{
            System.out.println("            EDGE COORDINATOR\n\n");
            IP = config.grabIP();
        } catch (UnknownHostException e) {
            System.err.println("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        }

        port = config.getPort("edgeCoordinator.port");

        // Try to create a serverSocket to listen to requests 
        try {
            listening = new ServerSocket(port);  
            System.out.println("Listening on port " + port);

            listening.setSoTimeout(5000);   // Since new connections probably won't be made too often

        } catch (Exception e) {
            System.err.println("Error creating Listening Socket on port " + port);
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }

    }
    public static void main(String[] args) {

        init();         // Begins the initalization process 

        boolean on = true;

        while(on){

            // Waits for a connection, when connection: start a new thread that will handle the message
            try{
                clientSocket = listening.accept(); // Accepts the incomming request
                Thread handlerThread = new Thread(new CoordClientHandler(clientSocket));
                handlerThread.start();
            } catch (SocketTimeoutException sto) {
                System.out.println("\nListening for new server...\n");
            } catch (IOException e){
                e.printStackTrace();
            } 
        }
    }
}