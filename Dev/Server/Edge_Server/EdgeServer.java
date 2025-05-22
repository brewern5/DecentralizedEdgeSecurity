package edge_server;

import java.io.*;
import java.net.*;
import java.util.*;

import config.Config;       // The configuration file for the entire network
import handler.ServerClientHandler;

public class EdgeServer {

    private static String IP;
    private static int port;

    private static ServerSocket listeningTop;       // The socket that will be listening to requests from the Edge Coordinator
    private static Socket sendingTop;               // The socket that will send messages to the Edge Coordinator

    private static ServerSocket listeningBottom;    // The socket that will be listening for Nodes
    private static Socket sendingBottom;            // The socket that will send messages to its Nodes

    /*
     *  Initalizes the Edge Server
     * 
     */

    public static void init() {

        Config config = new Config();

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

            output.println("Hello From the client!");

            String ack = input.readLine();

            if("ACK".equals(ack)){
                System.out.println("Received ACK from server!");
                // TODO : Do some acknowledgment Stuff here
            } else {
                System.out.println("Unexpected response: " + ack);
            }

            output.close();
            input.close();          // Close the port
            sendingTop.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create listening port for the coordinator
        try {
            listeningTop = new ServerSocket(config.getPort("edgeServerListeningTop.port"));
            listeningTop.setSoTimeout(5000);   
            System.out.println("Listening for coordinator on port " + port);
        } catch (Exception e) {
            System.err.println("Error creating Listening Socket on port " + port);
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }


        // Try to create a serverSocket to listen to requests from Nodes
        try {
            listeningBottom = new ServerSocket(config.getPort("edgeServerListeningBottom.port"));
            listeningTop.setSoTimeout(1000);  
            System.out.println("Listening for nodes on port " + port);
        } catch (Exception e) {
            System.err.println("Error creating Listening Socket on port " + port);
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }
    }
    public static void main(String[] args) {

        init();         // Begins the initalization process 

        boolean on = true;

        while (on) {

            // Listener for the coordinator
            try{
                Socket coordinator = listeningTop.accept(); // Accepts the incomming request
                Thread handlerThread = new Thread(new ServerClientHandler(coordinator)); // sends the message to a handler
                handlerThread.start(); // Begins the new thread
            } catch (SocketTimeoutException sto) {
                System.out.println("\nListening for new server...\n");
            } catch (IOException e){
                e.printStackTrace();
            } 

            // Listener for the coordinator
            try{
                Socket node = listeningBottom.accept(); // Accepts the incomming request
                Thread handlerThread = new Thread(new ServerClientHandler(node)); // sends the message to a handler
                handlerThread.start(); // Begins the new thread
            } catch (SocketTimeoutException sto) {
                System.out.println("\nListening for new server...\n");
            } catch (IOException e){
                e.printStackTrace();
            } 

        }
        
    }
}