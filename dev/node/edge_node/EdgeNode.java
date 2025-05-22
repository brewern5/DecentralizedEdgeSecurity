package edge_node;

import java.io.*;
import java.net.*;
import java.util.*;

import config.Config;       // The configuration file for the entire network

public class EdgeNode {

    private static String IP;
    private static int port;

    private static ServerSocket listening;      // The socket that will be listening to requests from the Edge server.
    private static Socket sending;              // The socket that will send messages to edge server

    private static Socket clientSocket;

    /*
     *  Initalizes the Edge Node
     * 
     */

    public static void init() {

        Config config = new Config();

        // try/catch to generate the IP from ./Config.java - Throws UnknownHostException if it cannot determine the IP
        try{
            System.out.println("            EDGE NODE\n\n");
            IP = config.grabIP();
        } catch (UnknownHostException e) {
            System.err.println("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        }

        port = config.getPort("edgeNode.port");

        // Try to create a serverSocket to listen to requests 
        try {
            listening = new ServerSocket(port);     //datagramSocket for UDP
            System.out.println("Listening on port " + port);
        } catch (Exception e) {
            System.err.println("Error creating Listening Socket on port " + port);
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }
        // The sending port. Will come back to this
        /* 
        try (Socket socket = new Socket(IP, port);
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String message = "Hello from Edge Node!";
            output.println(message);
            System.out.println("Edge Node sent: " + message);

            // If server replies
            // String response = input.readLine();
            // System.out.println("Edge Node received: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
    public static void main(String[] args) {

        init();         // Begins the initalization process 

        boolean on = true;

        while(on){
            

            try{
                // Thread for client 
                // Send ack back on that same connection line
                // THREADS!
                // Define packet structure
                // Create seperate class for the packet
                    // Instantiate a new packet class and will populate that instance
                    // Create a packet DTO
                    // DTO (Look into this)
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String line;

            } catch (IOException e) {
                e.printStackTrace();
            }
             

        }
        
    }
}