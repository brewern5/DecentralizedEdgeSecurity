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

// Packet Structure
import packet.ServerPacketType;
import packet.ServerPacket;

// This will allow for Jsonification of packets before sending
import com.google.gson.Gson;

public class EdgeServer {

    private static Gson gson = new Gson();      // Instantiate the object that will Jsonify our packets

    private static String IP;      // The IP of this devices

    private static ServerListener coordinatorListener;  // The socket that will be listening to requests from the Edge Coordinator

    private static Socket coordinatorSender;        // The socket that will send messages to the Edge Coordinator

    private static ServerListener nodeListener;     // The socket that will be listening for Nodes

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

        /* 
        Create the inital connection with the coordinator. This Try/Catch will try to create the inital
        connection with the Coordinator. 
        In this it will try and create an Initalization packet, which will send it's preffered receiving port
        to the Coordinator in order to store that into it's config file
        */
        try {
            System.out.println("\n\n\t\tSending initalization packet to the Coordinator\n\n");

            coordinatorSender = new Socket(
                config.getIPByKey("Coordinator.IP"), 
                config.getPortByKey("Coordinator.sendingPort")
            );

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

            // This is where the server will wait for a proper Ack from the coordinator - if not received, will retry 3 times
            while(!ackReceived){
                String json = initPacket.toDelimitedString();        // jsonifies the packet to be sent

                // Initalized inside nested control structure
                ServerPacket responsePacket;

                // Creates the input and the output for the socket.
                PrintWriter output = new PrintWriter(
                    coordinatorSender.getOutputStream(), 
                    true
                );
                // Sends the packet through the socket to the Coordinator
                output.println(json);

                // Will be where the response is read from
                BufferedReader input = new BufferedReader(
                    new InputStreamReader(coordinatorSender.getInputStream())
                );

                // Retrieves the response packet from the Coordinator
                String response = input.readLine();

                // Checks if the payload is properly terminated. If not, the packet is incomplete or an unsafe packet was sent
                try{
                    if(!response.endsWith("||END||") || response == null){
                        throw new IllegalArgumentException("\n\nPayload not properly terminated. \n\tPossible Causes:\n\t\t- Incomplete Packet\n\t\t- Unsafe Packet\n");
                    }
                    System.out.println("\t\t\tResponse recieved\n");
                    // If true, the packet will remove the delimiter so it can properly deserialize the Json (Since ||END|| is not json)
                    response = response.substring(
                        0, 
                        response.length() - "||END||".length()
                    );

                    // Will handle the response packet and look for errors, if packetType = ACK then good to contiue initalization
                    responsePacket = gson.fromJson(response, ServerPacket.class);

                    // Print out the packet 
                    System.out.println(
                        "Sender: \t" + responsePacket.getSender() 
                        + "\n\nPacket Type: \t" + responsePacket.getPacketType() 
                        + "\n\nPayload: \t" + responsePacket.getPayload()  
                        + "\n\n"
                    );

                    // If the packet type is a ACK packet - then it is a good connection made and the server will close this socket.
                    if (responsePacket.getPacketType() != ServerPacketType.ACK) {
                        throw new IllegalStateException("\n\nExpected ACK packet, but received: " + responsePacket.getPacketType());
                    }

                    ackReceived = true; // Break out of while loop to contiune initalization
                } catch(IllegalArgumentException illegalArg) {
                    // The exception if the packet is empty or has no termination 
                    System.err.println("Error: " + illegalArg.getMessage());
                    illegalArg.printStackTrace();
                } catch(IllegalStateException illegalState) {
                    // The exception if the packet is not of the type ACK
                    System.err.println("Error: " + illegalState.getMessage());
                    illegalState.printStackTrace();
                } finally {
                    // Always close the ports no matter the success status. 
                    output.close();             //
                    input.close();              // Close the port, we cannot reuse the same socket connection if a retry is needed
                    coordinatorSender.close();  //
                }

                // If the attempt limit is reached the server will shutdown
                if (attempts == maxRetries) {
                    System.err.println("\n\nAttempt limit reached trying to recieve ACK");
                    Thread.sleep(1000);
                    System.err.println("---");
                    Thread.sleep(1000);
                    System.err.println("Shutting down!");
                    System.exit(0);
                }
                // Retry the connection - must reopen the socket to create a new connection
                else if(attempts < maxRetries && !ackReceived) {
                    System.err.println("\nFailed to recieve ACK - retrying...\n\n");
                    // recreate the socket since we have to restablish the connection
                    coordinatorSender = new Socket(
                            config.getIPByKey("Coordinator.IP"),
                            config.getPortByKey("Coordinator.sendingPort"));
                    // Wait to retry and increment attempts
                    Thread.sleep(1000);
                }
                // Inc attemps 
                attempts++;
            }
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