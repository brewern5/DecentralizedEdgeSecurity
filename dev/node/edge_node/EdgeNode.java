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
package edge_node;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import node_handler.NodeServerHandler;
import node_listener.NodeListener;
import node_packet.NodePacket;
import node_packet.NodePacketType;
import node_sender.NodePacketSender;

// This will allow for Jsonification of packets before sending
import com.google.gson.Gson;

import node_config.NodeConfig;

public class EdgeNode {

    private static Gson gson = new Gson();  

    private static String IP;

    private static NodeListener serverListener;            // The socket that will be listening to requests from the Edge server.
    private static NodePacketSender serverSender;              // The socket that will send messages to edge server

    /*
     *  Initalizes the Edge Node
     * 
     */

    public static void init() {

        NodeConfig config = new NodeConfig();

        // try/catch to generate the IP from ../config/Config.java - Throws UnknownHostException if it cannot determine the IP
        try{
            System.out.println("\t\tEDGE NODE\n\n");
            IP = config.grabIP();
        } catch (UnknownHostException e) {
            System.err.println("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        }

        // Try to connect to the server
        try {
            // Create the main sender for the packets sent to the Coordinator and stores the relevant socket information
            serverSender = new NodePacketSender(
                config.getIPByKey("Server.IP"), 
                config.getPortByKey("Server.listeningPort")
            );

            NodePacket initPacket = new NodePacket(
                NodePacketType.INITIALIZATION,         // Packet type
                "EdgeNode",                     // Sender
                "Node.listeningPort:6001"      // Payload
            );

            // Sends the packet through the sender
            serverSender.send(initPacket);

        } catch(Exception e){
            e.printStackTrace();
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
            System.err.println(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("Node.listeningPort")
            );
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }
        
    }
    public static void main(String[] args) {

        init();         // Begins the initalization process 

        Thread serverThread = new Thread(serverListener);
        serverThread.start();

        boolean on = true;
        while(on){
            
        }       
    }
}