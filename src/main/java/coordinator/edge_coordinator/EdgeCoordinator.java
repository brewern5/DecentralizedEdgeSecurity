/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the top of the hierarchy in our edge network, this is the connection point between the
 *      hierarchy and the cloud. Since this is the top of the network, it's instantiation is the highest
 *      priority, and will be started first.
 * 
 *      It's only direct child is the Server. This coordinator may potentially have multiple so that will
 *      need to be considered
 * 
 *      When started the Coordinator will connected to 'coordinatorConfig.properties' through the 
 *      coordinatorConfig.java file as the 'config' object. This will allow for the getting of the IP address
 *      and the Port(s).
 * 
 *      After these items are grabbed, a listener for the server will be instantiated to be used later. This
 *      will finish the initalization process. Once this is done, the listener will be thrown into the thread
 *      to be constantly ran seperate of this project
 */
package coordinator.edge_coordinator;

import java.net.UnknownHostException;
import java.util.Scanner;

import coordinator.coordinator_config.CoordinatorConfig;

import coordinator.coordinator_listener.CoordinatorListener;

import coordinator.coordinator_packet.CoordinatorPacket;
import coordinator.coordinator_packet.CoordinatorPacketType;
import coordinator.coordinator_packet.coordinator_packet_class.*;

import coordinator.coordinator_sender.CoordinatorPacketSender;

public class EdgeCoordinator {

    private static String IP;

    private static CoordinatorListener serverListener;  // The socket that will be listening to requests from the Edge server.
    private static CoordinatorPacketSender serverSender;  // The socket that will send messages to edge server

    /*
     *  Initalizes the Node Coordinator - This will be the first thing that runs when the Node Coordinator is started up
     * 
     */

    public static void init() {

        // Create a config object to access the properties file
        CoordinatorConfig config = new CoordinatorConfig();

        // try/catch to generate the IP from ./Config.java - Throws UnknownHostException if it cannot determine the IP
        try{
            System.out.println("\t\tEDGE COORDINATOR\n\n");
            // Get the IP address for this coordinator
            IP = config.grabIP(); 
        } catch (UnknownHostException e) {
            System.err.println("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        }

        // Try to create a serverSocket to listen to requests 
        try {
            // Construct the listener - sending the
            serverListener = new CoordinatorListener(
                config.getPortByKey("Coordinator.listeningPort"),
                 5000
            );  


        } catch (Exception e) {
            System.err.println(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("Coordinator.listeningPort")
            );
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }

        try{

            // Create the sender to the server
            serverSender = new CoordinatorPacketSender(
                config.grabIP(), 
                config.getPortByKey("Server.listeningPort")
            );

        } catch (Exception e) {
             System.err.println("Error creating Sending Socket");
            e.printStackTrace();
        }

    }
    public static void main(String[] args) {

        init();         // Begins the initalization process 

        // Instaniate the thread and send the serverListener to it
        Thread listeningThread = new Thread(serverListener);

        // Start the thread
        listeningThread.start();

        // TODO: DEMO
        Scanner in = new Scanner(System.in);

        boolean on = true;
        while(on){
            // TODO: Stuff
            System.out.println("Manually send message to server: ");
            String message = in.nextLine();

            if(!message.isEmpty()) {
                CoordinatorPacket messagePacket = new CoordinatorGenericPacket(
                    CoordinatorPacketType.MESSAGE,
                    "Coordinator",
                    message
                );
                serverSender.send(messagePacket);
            }
        }
        in.close();
    }
}