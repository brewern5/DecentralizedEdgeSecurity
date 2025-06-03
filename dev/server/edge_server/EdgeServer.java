package edge_server;

import java.io.*;
import java.net.*;
import java.util.*;

import config.ServerConfig;       // The configuration file for the server, holds the IPs and Ports for different layers 

// Configures the listeners
import handler.ServerNodeHandler;
import listeners.ServerListener;

// Packet Structure
import packet.ServerPacketType;
import packet.ServerPacket;

// Jsonify the packet before sending
import com.google.gson.Gson;

public class EdgeServer {

    private static Gson gson = new Gson();

    private static String IP;
    private static int port;

    private static ServerListener coordinatorListener;  // The socket that will be listening to requests from the Edge Coordinator

    private static Socket coordinatorSender;            // The socket that will send messages to the Edge Coordinator

    private static ServerListener nodeListener;         // The socket that will be listening for Nodes

    private static Socket nodeSender;                   // The socket that will send messages to its Nodes

    /*
     *  Initalizes the Edge Server
     * 
     */

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

        // Create the inital connection with the coordinator
        try {
            System.out.println("\n\n\t\tSending initalization packet to the Coordinator\n\n");

            coordinatorSender = new Socket(config.getIPByKey("Coordinator.IP"), config.getPortByKey("Coordinator.sendingPort"));

            // Create the initalization packet
            ServerPacket initPacket = new ServerPacket(
                ServerPacketType.INITIALIZATION,       // Packet type
                "EdgeServer",                   // Sender
                "server.listeningPort:5003"    // Payload
            );


            // If the acknowledgement is not recieved then it will try 2 more times and if it still can't connect then it will shutdown
            int maxRetries = 3;
            int attempts = 0;
            boolean ackReceived = false;

            while(!ackReceived){
                String json = initPacket.toString();        // jsonifies the packet to be sent

                PrintWriter output = new PrintWriter(coordinatorSender.getOutputStream(), true);
                BufferedReader input = new BufferedReader(new InputStreamReader(coordinatorSender.getInputStream()));

                output.println(json);

                String response = input.readLine();
                
                // Will handle the response packet and look for errors, if packetType = ACK then good to contiue initalization
                ServerPacket ackPacket = gson.fromJson(response, ServerPacket.class);
                System.out.println("Sender: \n\t" + ackPacket.getSender() + "\n\nPacket Type: \n\t" + ackPacket.getPacketType() + "\n\nPayload: \n\t" + ackPacket.getPayload()  + "\n");
                
                if(ackPacket.getPacketType() == ServerPacketType.ACK) {
                    output.close();             //
                    input.close();              // Close the port
                    coordinatorSender.close();  //

                    ackReceived = true;     // Break out of while loop
                } else {
                    System.err.println("Failed to recieve ACK - retrying");
                    if(attempts >= maxRetries) {
                        System.err.println("Attempt limit met trying to recieve ACK - shutting down");
                        // TODO: shutdown
                    }
                    // Wait to retry and increment attempts
                    Thread.sleep(1000);
                    attempts++;
                }
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * 
         *                  Listeners
         * 
         */

        // Instantiate a listening port for the coordinator, will need to run this on a thread
        try {
            coordinatorListener = new ServerListener(config.getPortByKey("CoordinatorListener.listeningPort"), 5000);
        } catch (Exception e) {
            System.err.println("Error creating Listening Socket on port " + config.getPortByKey("CoordinatorListener.listeningPort"));
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }

        // Instantiate a listening port for the Nodes, will need to run this on a thread
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