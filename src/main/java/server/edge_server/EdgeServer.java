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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_config.ServerConfig;

import server.server_listener.ServerListener;

import server.server_packet.server_packet_class.*;
import server.server_packet.*;

import server.server_connections.*;
import server.server_connections.server_connection_manager.*;

public class EdgeServer {

    private static final Logger logger = LogManager.getLogger(EdgeServer.class);

    private static volatile String serverId = null;

    private static String IP;      // The IP of this devices

    private static ServerListener coordinatorListener;  // The socket that will be listening to requests from the Edge Coordinator
    private static ServerListener nodeListener;     // The socket that will be listening for Nodes

    private static ServerConnectionManager nodeConnectionManager;   // This will manage all connections and check keep alive
    private static ServerConnectionManager coordinatorConnectionManager;

    // Timer components
    private static ScheduledExecutorService timerScheduler; // The timer that will send out to keepAlives to the coordinator

    /*
     *  Initalize the Edge Server
     */
    // This is the function that will be called first that will have the inital handshake between the Coordinator
    public static void init() {

        ServerConfig config = new ServerConfig();

        // Create the connection manager
        nodeConnectionManager = ServerNodeConnectionManager.getInstance();

        // try to generate the IP from the machines IP - Throws UnknownHostException if it cannot determine
        try{
            System.out.println("\t\tEDGE SERVER");
            IP = config.grabIP();
            logger.info("Starting Server at " + IP + ".");
        } catch (UnknownHostException e) {
            System.err.println("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        }

        /*          Try to create senders        */
        try{

            coordinatorConnectionManager = ServerCoordinatorConnectionManager.getInstance();
            
            coordinatorConnectionManager.addConnection(
                new ServerConnectionInfo(
                    "1",  // TEMP Will update when get the ack respones
                    config.getIPByKey("Coordinator.IP"), 
                    config.getPortByKey("Coordinator.sendingPort"),
                    ServerPriority.CRITICAL
                )
            );

            LinkedHashMap<String, String> payload = new LinkedHashMap<>();
            payload.put("Server.listeningPort", "5003");

            // Create the initalization packet
            ServerPacket initPacket = new ServerGenericPacket(
                ServerPacketType.INITIALIZATION, 
                getServerId(),                   
                payload
            );  

            coordinatorConnectionManager.getConnectionInfoById("1").createSender();
            coordinatorConnectionManager.getConnectionInfoById("1").send(initPacket);
            
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: Log critical error
        }

        /*
         *          Listeners
         */

        // Instantiate a listening port for the coordinator
        try {
            coordinatorListener = new ServerListener(
                config.getPortByKey("CoordinatorListener.listeningPort"), 
                5000,
                "coordinator"
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
                 1000,
                 "node"
            );
        } catch (Exception e) {
            System.err.println(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("NodeListener.listeningPort")
            );
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }

        /*
         * 
         *      Try and create Timers
         * 
         */

        try {
            initializeTimers();
        } catch (Exception e) {
            logger.error("Error creating Timers: \n" + e.getStackTrace());
        }
    }

    /*
     * 
     *      ID assignment 
     * 
     */

    // Thread-safe setter for ID assignment
    public static synchronized void setServerId(String id) {
        serverId = id;
        logger.info("Server ID assigned: " + id);
    }

    public static synchronized String getServerId() {
        return serverId;
    }

    private static void initializeTimers() {
        timerScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "EdgeServer-Timer");
            t.setDaemon(true);
            return t;
        });

        // Schedules the sending for the keepAlive packet every 30 seconds
        timerScheduler.scheduleAtFixedRate(() -> {
            
        }, 30, 30, TimeUnit.SECONDS);
        logger.info("Timer for sending keep Alive packets created!");

        // Schedules overdue packet check every 20 seconds
        timerScheduler.scheduleAtFixedRate(() -> {
            nodeConnectionManager.checkExpiredConnections();
        }, 20, 20, TimeUnit.SECONDS);
        logger.info("Timer for checking Expired connections created!");

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

            System.out.println("\nManually Send Message: ");
            String message = in.nextLine();

            if(!message.isEmpty()) {
                ServerPacket messagePacket = new ServerGenericPacket(
                    ServerPacketType.MESSAGE,
                    getServerId(),
                    message
                );

                System.out.println("Send message to Node or to Coordinator?");
                String recipient = in.nextLine();

                if(recipient.equals("node") || recipient.equals("Node")) {

                    String[] nodeIdArray = nodeConnectionManager.getAllIds();

                    HashMap<Integer, String> nodes = new HashMap<>();

                    boolean hasNum = false;

                    int trys = 0;

                    while(!hasNum) {
                        nodes.clear();
                        System.out.println("Chose the Node to send to based on the Number next to it!");
                        for(int i = 1; i < nodeIdArray.length + 1; i++) {
                            System.out.println(i + " : " + nodeIdArray[i-1]);
                            nodes.put(i, nodeIdArray[i-1]);
                        }
                        int nodeNum = in.nextInt();
                        if(!nodes.get(nodeNum).isEmpty()) {
                            nodeConnectionManager.getConnectionInfoById(nodes.get(nodeNum))
                                .createSender();
                            nodeConnectionManager.getConnectionInfoById(nodes.get(nodeNum))
                                .send(messagePacket);
                            hasNum = true;
                        }
                        else if(trys > 2){
                            System.out.println("Try again next time loser!");
                            hasNum = true;
                        }
                        trys++;
                    }

                } else if(recipient.equals("coordinator") || recipient.equals("Coordinator")) {
                    //coordinatorSender.send(messagePacket);
                }
                else {
                    System.out.println("Unknown Recipient! \nYour input was: " + recipient + "\nTry again.");
                }
            }
        }
        in.close();
    }
}