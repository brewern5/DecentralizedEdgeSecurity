/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the main thread for the edge node (1st tier) of the hierarchy.
 *      This will have multiple instances per server and will switch between different servers
 *      
 *      In this file, an INITALIZATION packet is sent to the inital Server to provide 
 *      it with the preferred listening port for further commands. This is part of the
 *      node Initalization process that will establish the connection to the server
 *      
 *      If the initalization is successful, then a listener will be created for the 
 *      server. A sender will also be for the server
 */
package node.edge_node;

import java.net.UnknownHostException;

import java.util.LinkedHashMap;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import node.node_listener.NodeListener;

import node.node_packet.*;
import node.node_packet.node_packet_class.*;

import node.node_sender.NodePacketSender;

import node.node_config.NodeConfig;

public class EdgeNode {

    private static volatile String nodeId = null;

    private static String IP;

    private static NodeListener serverListener;            // The socket that will be listening to requests from the Edge server.
    private static NodePacketSender serverSender;          // The socket that will send messages to edge server

    // Each class can have its own logger instance
    private static final Logger logger = LogManager.getLogger(EdgeNode.class);

    /*
     *  Initalizes the Edge Node
     * 
     */

    public static void init() {

        NodeConfig config = new NodeConfig();

        // try/catch to generate the IP from ../config/Config.java - Throws UnknownHostException if it cannot determine the IP
        try{
            IP = config.grabIP();
            logger.info("Starting Node at " + IP + ".");
        } catch (UnknownHostException e) {
            logger.error("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        }

        // Try to connect to the server
        try {
            // Create the main sender for the packets sent to the Coordinator and stores the relevant socket information
            serverSender = new NodePacketSender(
                config.getIPByKey("Server.IP"), 
                config.getPortByKey("Server.listeningPort")
            );

            LinkedHashMap<String, String> payload = new LinkedHashMap<>();
            payload.put("Node.listeningPort", "6001");
            
            NodePacket initPacket = new NodeGenericPacket(
                NodePacketType.INITIALIZATION, 
                getNodeID(),                            
                payload        
            );
            
            logger.debug("Sending initalization packet. Packet: " + initPacket.toJson());

            // Sends the packet through the sender
            serverSender.send(initPacket);

        } catch(Exception e){
            logger.error("Error Sending Initalization Packet: " + e.getStackTrace());
        }

        /*
         *          Listeners
         */

        try {
            serverListener = new NodeListener(
                config.getPortByKey("Node.listeningPort"), 
                2000
            );
        } catch (Exception e) {
            logger.error(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("Node.listeningPort")
                + e.getStackTrace()
            );
            // TODO: try to grab new port if this one is unavailable
        }
    }

    /*
     * 
     *      ID assignment 
     * 
     */

    // Thread-safe setter for ID assignment
    public static synchronized void setNodeID(String id) {
        nodeId = id;
        logger.info("Node ID assigned: " + id);
    }

    public static String getNodeID() {
        return nodeId;
    }

    /*
     * 
     *      Main Loop
     * 
     */

    public static void main(String[] args) {

        init();         // Begins the initalization process 

        Thread serverThread = new Thread(serverListener);
        serverThread.start();

        // TODO: DEMO
        Scanner in = new Scanner(System.in);

        boolean on = true;
        while(on){
            
            // TODO: DEMO
            System.out.println("Manually Send Message to Server: ");
            String message = in.nextLine();

            if(!message.isEmpty()) {
                NodePacket messagePacket = new NodeGenericPacket(
                    NodePacketType.MESSAGE,
                    getNodeID(),
                    message
                );

                serverSender.send(messagePacket);
            }
        }       
        in.close();
    }
}
