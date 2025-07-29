/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the main thread of the Server (2nd tier) of the hierarchy.
 *      The server is the middle man that connects the Node layer (bottom or 1sts tier)
 *      to the coordinator layer (top or the 3rd tier)
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
package server.edge_server;

import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.logging.*;

import server.server_config.ServerConfig;
import server.server_listener.ServerListener;

import server.server_packet.server_packet_class.*;
import server.server_packet.*;

import server.server_sender.ServerPacketSender;

public class EdgeServer {

    private static String IP;      // The IP of this devices

    private static ServerListener coordinatorListener;  // The socket that will be listening to requests from the Edge Coordinator
    private static ServerListener nodeListener;     // The socket that will be listening for Nodes

    private static ServerPacketSender coordinatorSender;        // The socket that will send messages to the Edge Coordinator
    private static ServerPacketSender nodeSender;       // The socket that will send messages to its Nodes

    /*
     *  Initalize the Edge Server
     */
    // This is the function that will be called first that will have the inital handshake between the Coordinator
    public static void init() {

        ServerConfig config = new ServerConfig();

        // try to generate the IP from the machines IP - Throws UnknownHostException if it cannot determine
        try{
            System.out.println("\t\tEDGE SERVER\n\n");
            IP = config.grabIP();
            //logger.logEvent();
        } catch (UnknownHostException e) {
            System.err.println("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        }

        /*          Try to create senders        */
        try{
            // Create the main sender for the packets sent to the Coordinator and stores the relevant socket information
            coordinatorSender = new ServerPacketSender(
                config.getIPByKey("Coordinator.IP"), 
                config.getPortByKey("Coordinator.sendingPort")
            );

            System.out.println("\n\n\t\tSending initalization packet to the Coordinator\n\n");

            LinkedHashMap<String, String> payload = new LinkedHashMap<>();
            payload.put("Server.listeningPort", "5003");

            // Create the initalization packet
            ServerPacket initPacket = new ServerGenericPacket(
                ServerPacketType.INITIALIZATION, 
                "EdgeServer",                   
                payload
            );

            // Sends the packet through the sender
            coordinatorSender.send(initPacket);
            
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: Log critical error
        }

        /*
         *                  Listeners and node sender
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
            System.err.println(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("NodeListener.listeningPort")
            );
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }

        // TODO: Create a reactive/dynamic sender for each instance of the node
        try{
            nodeSender = new ServerPacketSender(
                config.getIPByKey("Node.IP"), 
                config.getPortByKey("Node.sendingPort")
            );
        } catch (Exception e) {
            System.err.println("Error creating Node Sender on port ");
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {

        init();         // Begins the initalization process 

        Thread coordinatorThread = new Thread(coordinatorListener); // Starts listening for messages on the top
        coordinatorThread.start();

        Thread serverThread = new Thread(nodeListener);  // Starts listening for messages on the bottom
        serverThread.start();

        // DEMO
        Scanner in = new Scanner(System.in);

        boolean on = true;
        while (on) {

            System.out.println("\n\nManually Send Message: ");
            String message = in.nextLine();

            if(!message.isEmpty()) {
                ServerPacket messagePacket = new ServerGenericPacket(
                    ServerPacketType.MESSAGE,
                    "Server",
                    message
                );

                System.out.println("Send message to Node or to Coordinator?");
                String recipient = in.nextLine();

                if(recipient.equals("node") || recipient.equals("Node")) {
                    nodeSender.send(messagePacket);
                } else if(recipient.equals("coordinator") || recipient.equals("Coordinator")) {
                    coordinatorSender.send(messagePacket);
                }
                else {
                    System.out.println("Unknown Recipient! \nYour input was: " + recipient + "\nTry again.");
                }
            }
        }
        in.close();
    }
}