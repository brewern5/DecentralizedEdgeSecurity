package edge_node;

import java.io.*;
import java.net.*;
import java.util.*;

import config.NodeConfig;               // The configuration file for the entire network
import handler.NodeServerHandler;   

import listeners.NodeListener;

public class EdgeNode {

    private static String IP;

    private static NodeListener listener;            // The socket that will be listening to requests from the Edge server.
    private static Socket sending;              // The socket that will send messages to edge server

    /*
     *  Initalizes the Edge Node
     * 
     */

    public static void init() {

        NodeConfig config = new NodeConfig();

        // try/catch to generate the IP from ../config/Config.java - Throws UnknownHostException if it cannot determine the IP
        try{
            System.out.println("            EDGE NODE\n\n");
            IP = config.grabIP();
        } catch (UnknownHostException e) {
            System.err.println("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        }

        // Try to connect to the edge server
        try {
            sending = new Socket(config.getIPByKey("Server.IP"), config.getPortByKey("Server.listeningPort"));

            PrintWriter output = new PrintWriter(sending.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(sending.getInputStream()));

            output.println("This is the edge Node");

            // Read the response
            String line = input.readLine();
            if(line != null){
                System.out.println("Response: \n\t");
                System.out.println(line);
            }

            output.close();         //
            input.close();          // Close the port
            sending.close();        //

        } catch(Exception e){
            e.printStackTrace();
        }

        // Try to create a serverSocket to listen to requests 
        try {
            listener = new NodeListener(config.getPortByKey("Node.listeningPort"), 2000);
        } catch (Exception e) {
            System.err.println("Error creating Listening Socket on port " + config.getPortByKey("Node.listeningPort"));
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }
        
    }
    public static void main(String[] args) {

        init();         // Begins the initalization process 

        Thread serverThread = new Thread(listener);
        serverThread.start();

        boolean on = true;
        while(on){
            // Thread for client 
                // Send ack back on that same connection line
                // THREADS!
                // Define packet structure
                // Create seperate class for the packet
                    // Instantiate a new packet class and will populate that instance
                    // Create a packet DTO
                    // DTO (Look into this)
            // Listen for the Server
        }       
    }
}