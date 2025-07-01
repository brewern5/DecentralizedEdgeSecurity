/*
 *      Author: Nate Brewer
 * 
 *      This is the main thread of the Server (2nd tier) of the hierarchy.
 *      The server is the middle man that connects the Node layer (bottom or 3rd tier)
 *      to the coordinator layer (top or the 1st tier)
 *      
 *      In this file, an INITALIZATION packet is sent to the Coordinator to provide 
 *      it with the preferred listening port for further commands. This is part of the
 *      server Initalization process that will establish the connection to the Coordinator
 *      
 *      If the initalization is successful, then the server will create listeners for
 *      both the Node layer and the coordinator layer, on seperate ports. These listeners
 *      will be started on different threads.
 *      
 */
package edge_server;

import java.io.*;
import java.net.*;
import java.util.*;

import config.ServerConfig;       // The configuration file for the server, holds the IPs and Ports for different layers 

// Configures the listeners
import handler.ServerNodeHandler;
import listeners.ServerListener;

// Configures the senders
import sender.*;
import sender.sub_classes.*;
// Packet Structure
import packet.ServerPacketType;
import packet.ServerPacket;

// This will allow for Jsonification of packets before sending
import com.google.gson.Gson;

public class EdgeServer {

    private static Gson gson = new Gson();      // Instantiate the object that will Jsonify our packets

    private static String IP;      // The IP of this devices

    private static ServerListener coordinatorListener;  // The socket that will be listening to requests from the Edge Coordinator
    private static ServerListener nodeListener;     // The socket that will be listening for Nodes

    private static PacketSender coordinatorSender;        // The socket that will send messages to the Edge Coordinator
    private static Socket nodeSender;       // The socket that will send messages to its Nodes

    /*
     *  Initalize the Edge Server
     */
    // This is the function that will be called first that will have the inital handshake between the Coordinator
    public static void init() {

        ServerConfig config = new ServerConfig();

        // try to generate the IP from the machines IP - Throws UnknownHostException if it cannot determine
        try{
            System.out.println("            EDGE SERVER\n\n");
            IP = config.grabIP();
        } catch (UnknownHostException e) {
            System.err.println("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        }

        /*          Try to create senders        */
        try{

            Socket tempSocket = new Socket(
                    config.getIPByKey("Coordinator.IP"), 
                    config.getPortByKey("Coordinator.sendingPort")
                );

            // Create the main sender for the packets sent to the Coordinator
            coordinatorSender = new PacketSender();
            coordinatorSender.setSocket(tempSocket);
            //HeartbeatSender heartbeat = new HeartbeatSender(tempSocket);

        } catch(Exception e) {

        }

        try {
            System.out.println("\n\n\t\tSending initalization packet to the Coordinator\n\n");

            InitializationSender initPacketSender = new InitializationSender();
            
            initPacketSender.send();
            
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: Log critical error
        }

        /*
         *                  Listeners
         */

        // Instantiate a listening port for the coordinator
        try {
            coordinatorListener = new ServerListener(
                config.getPortByKey("CoordinatorListener.listeningPort"), 
                5000
            );
        } catch (Exception e) {
            System.err.println(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("CoordinatorListener.listeningPort")
            );
            
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }

        // Instantiate a listening port for the Nodes
        try {
            nodeListener = new ServerListener(
                config.getPortByKey("NodeListener.listeningPort"),
                 1000
            );
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