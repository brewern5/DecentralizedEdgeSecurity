package edge_coordinator;

import java.io.*;
import java.net.*;
import java.rmi.ConnectIOException;
import java.util.*;

import config.CoordConfig;       // The configuration file for the entire network
import handler.CoordClientHandler;

import listeners.Listener;

public class EdgeCoordinator {

    private static String IP;

    private static Listener listener;      // The socket that will be listening to requests from the Edge server.
    private static Socket sending;              // The socket that will send messages to edge server

    private static Socket clientSocket;

    /*
     *  Initalizes the Node Coordinator - This will (probably) be the first thing that runs when the Node Coordinator is started up
     * 
     */

    public static void init() {

        CoordConfig config = new CoordConfig();

        // try/catch to generate the IP from ./Config.java - Throws UnknownHostException if it cannot determine the IP
        try{
            System.out.println("            EDGE COORDINATOR\n\n");
            IP = config.grabIP();
        } catch (UnknownHostException e) {
            System.err.println("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        }

        // Try to create a serverSocket to listen to requests 
        try {
            listener = new Listener(config.getPort("edgeCoordinator.port"), 5000);  
        } catch (Exception e) {
            System.err.println("Error creating Listening Socket on port " + config.getPort("edgeCoordinator.port"));
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }

    }
    public static void main(String[] args) {

        init();         // Begins the initalization process 

        Thread listeningThread = new Thread(listener);
        listeningThread.start();

        boolean on = true;
        while(on){


        }
    }
}