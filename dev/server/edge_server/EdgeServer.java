package edge_server;

import java.io.*;
import java.net.*;
import java.util.*;

import config.ServerConfig;       // The configuration file for the entire network
import handler.ServerNodeHandler;

import listeners.ServerListener;
import packet.ServerPacketType;
import packet.ServerPacket;

public class EdgeServer {

    private static String IP;
    private static int port;

    private static ServerListener coordinatorListener;        // The socket that will be listening to requests from the Edge Coordinator

    private static Socket coordinatorSender;            // The socket that will send messages to the Edge Coordinator

    private static ServerListener nodeListener;               // The socket that will be listening for Nodes

    private static Socket nodeSender;                   // The socket that will send messages to its Nodes

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
            coordinatorSender = new Socket(config.getIPByKey("Coordinator.IP"), config.getPortByKey("Coordinator.sendingPort"));

            // Create the initalization packet
            ServerPacket initPacket = new ServerPacket(
                ServerPacketType.INITIALIZATION,      // Packet type
                "EdgeServer",            // Sender
                "Requesting handshake"  // Payload
            );

            String json = initPacket.toString();

            PrintWriter output = new PrintWriter(coordinatorSender.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(coordinatorSender.getInputStream()));

            output.println(json);

            // TODO: Get ack somewhere in here

            output.close();
            input.close();          // Close the port
            coordinatorSender.close();

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
            coordinatorListener = new ServerListener(config.getPortByKey("CoordinatorListener.listeningPort"), 5000);
        } catch (Exception e) {
            System.err.println("Error creating Listening Socket on port " + config.getPortByKey("CoordinatorListener.listeningPort"));
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }

        // Try to create a serverSocket to listen to requests from Nodes
        try {
            nodeListener = new ServerListener(config.getPortByKey("NodeListener.listeningPort"), 1000);
        } catch (Exception e) {
            System.err.println("Error creating Listening Socket on port " + config.getPortByKey("NodeListener.listeningPort"));
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }
    }
    public static void main(String[] args) {

        init();         // Begins the initalization process 

        Thread coordinatorThread = new Thread(coordinatorListener); // Starts listening for messages on the top
        coordinatorThread.start();

        Thread bottomThread = new Thread(nodeListener);  // Starts listening for messages on the bottom
        bottomThread.start();

        boolean on = true;
        while (on) {
            // TODO : do something in this main thread's loop
        }
    }
}