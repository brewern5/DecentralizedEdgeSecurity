package edge_server;

import java.io.*;
import java.net.*;
import java.util.*;

import config.ServerConfig;       // The configuration file for the entire network
import handler.ServerClientHandler;

import listeners.Listener;

public class EdgeServer {

    private static String IP;
    private static int port;

    private static Listener listeningTop;       // The socket that will be listening to requests from the Edge Coordinator

    private static Socket sendingTop;           // The socket that will send messages to the Edge Coordinator

    private static Listener listeningBottom;    // The socket that will be listening for Nodes

    private static Socket sendingBottom;        // The socket that will send messages to its Nodes

    /*
     *  Initalizes the Edge Server
     * 
     */

    public static void init() {

        ServerConfig config = new ServerConfig();

        // try/catch to generate the IP from ./Config.java - Throws UnknownHostException if it cannot determine the IP
        try{
            System.out.println("            EDGE SERVER\n\n");
            IP = config.grabIP();
        } catch (UnknownHostException e) {
            System.err.println("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        }

        // Create the inital connection with the coordinator
        try {
            sendingTop = new Socket(config.getIPByKey("edgeCoordinator.IP"), config.getPort("edgeCoordinator.port"));

            PrintWriter output = new PrintWriter(sendingTop.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(sendingTop.getInputStream()));

            output.println("Hello From the Server!");

            output.close();
            input.close();          // Close the port
            sendingTop.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * 
         *                  Listeners
         * 
         */

        // Create listening port for the coordinator
        try {
            listeningTop = new Listener(config.getPort("edgeServerListeningTop.port"), 5000);
        } catch (Exception e) {
            System.err.println("Error creating Listening Socket on port " + config.getPort("edgeServerListeningTop.port"));
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }

        // Try to create a serverSocket to listen to requests from Nodes
        try {
            listeningBottom = new Listener(config.getPort("edgeServerListeningBottom.port"), 1000);
        } catch (Exception e) {
            System.err.println("Error creating Listening Socket on port " + config.getPort("edgeServerListeningBottom.port"));
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }
    }
    public static void main(String[] args) {

        init();         // Begins the initalization process 

        Thread topThread = new Thread(listeningTop);        // Starts listening for messages on the top
        topThread.start();

        Thread bottomThread = new Thread(listeningBottom);  // Starts listening for messages on the bottom
        bottomThread.start();

        boolean on = true;
        while (on) {


        }
        
    }
}