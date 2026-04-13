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
package components.server;

import java.net.SocketException;
import java.net.UnknownHostException;

//DEMO
/* 
import java.util.HashMap;
import java.util.Scanner;
*/ 

import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import components.server.connections.*;
import components.server.identity.ServerIdentity;
import components.server.listener.ServerListener;
import components.server.services.ServerClusterManager;
import components.server.config.ServerConfig;
import core.config.AbstractConfig;

import core.connection.ConnectionDto;
import core.connection.Priority;
import core.identity.AbstractTierIdentity;
import core.identity.TierRole;
import core.packet.AbstractPacket;
import core.packet.initalization.InitalizationPacketManager;

public class EdgeServer {

    private static final Logger logger = LogManager.getLogger(EdgeServer.class); 

    private static volatile String serverId = null;

    private static String IP;

    private static volatile AbstractTierIdentity instanceDTO;

    private static ServerListener coordinatorListener; 
    private static ServerListener nodeListener;     

    private static AbstractConfig config;

    private static ServerNodeConnectionManager nodeConnectionManager;
    private static ServerCoordinatorConnectionManager coordinatorConnectionManager;

    // Timer components
    private static ScheduledExecutorService timerScheduler;  

    /*
     *  Initalize the Edge Server
     */
    public static void init() {

        nodeConnectionManager = ServerNodeConnectionManager.getInstance("", "", "Server");

        try{
            IP = config.grabIP();
            logger.info("\t\tEDGE SERVER\tStarting Server at " + IP);
        } catch (UnknownHostException e) {
            logger.error("Error: Unable to determine local host IP address.\n" + e);
        } catch (SocketException e) {
            logger.error("Error: Unable to determine IP Address");
        }

        /*          Try to create senders        */
        try{

            coordinatorConnectionManager = ServerCoordinatorConnectionManager.getInstance("", "", "Server");
            
            coordinatorConnectionManager.addConnection(
                new ConnectionDto(
                    "1",  // TEMP Will update when get the ack respones
                    config.getIPByKey("Coordinator.IP"), 
                    config.getPortByKey("Coordinator.listeningPort"),
                    Priority.CRITICAL
                )
            );

            LinkedHashMap<String, String> payload = new LinkedHashMap<>();
            payload.put(
                "Server.listeningPort",
                String.valueOf(config.getPortByKey("Server.coordinatorListeningPort"))
            );

            AbstractPacket initPacket = new InitalizationPacketManager(
                "", 
                "", 
                "1", 
                "Server", 
                coordinatorConnectionManager, 
                IP
            )
            .createOutgoingPacket();

            initPacket.addPayload(payload);

            coordinatorConnectionManager.sendToConnection("1", initPacket);
            
            String assignedServerId = coordinatorConnectionManager.getInstanceId();
            setServerId(assignedServerId);
            
            nodeConnectionManager.setInstanceId(assignedServerId);
            logger.info("Updated nodeConnectionManager with server ID: {}", assignedServerId);

        } catch (Exception e) {
            logger.error("Error Sending Initalization Packet: " + e);
        }

        /*
         *          Listeners
         */
        try {
            coordinatorListener = new ServerListener(
                config.getPortByKey("Server.coordinatorListeningPort"), 
                5000,
                "coordinator"
            );
        } catch (Exception e) {
            logger.error(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("Server.coordinatorListeningPort")
                + "\n" + e
            );
            // TODO: try to grab new port if this one is unavailable
        }


        try {
            nodeListener = new ServerListener(
                config.getPortByKey("Server.nodeListeningPort"),
                 1000,
                 "node"
            );
        } catch (Exception e) {
            logger.error(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("Server.nodeListeningPort")
                + "\n" + e
            );
            // TODO: try to grab new port if this one is unavailable
        }
        /*
         *      Try and create Timers
         */
        try {
            initializeTimers();
        } catch (Exception e) {
            logger.error("Error creating Timers: \n" + e);
        }

        /*
         *     Create cluster ID 
         */

        try {
            ServerClusterManager.initializeClusterIdentity();
            nodeConnectionManager.setClusterId(ServerClusterManager.getClusterId());
        } catch (Exception e) {
            logger.error("Error creating Cluster ID: " + e);
        }
    }
    /*
     *      ID assignment 
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
            //t.setDaemon(true);
            return t;
        });
        // Schedules the sending for the keepAlive packet every 30 seconds
        timerScheduler.scheduleAtFixedRate(() -> {
            try {
                boolean keepAliveSent;

                keepAliveSent = coordinatorConnectionManager.sendKeepAlive();
                
                if(!keepAliveSent){
                    logger.error("Keep alive was not sent!");
                    //TODO: something to stop the keep alive sender
                }

            } catch(core.exception.KeepAliveException e) {
                logger.error("Keep-alive failed at stage: {} for connection: {} - {}", 
                           e.getStage(), e.getConnectionId(), e.getMessage());
                
                if (e.isSendFailure()) {
                    logger.error("Failed to send keep-alive packet - network or socket issue");
                } else if (e.isAckFailure()) {
                    if (e.getStage() == core.exception.KeepAliveException.FailureStage.ACK_NOT_RECEIVED) {
                        logger.error("Keep-alive sent but ACK not received - coordinator may be down");
                    } else if (e.getStage() == core.exception.KeepAliveException.FailureStage.ACK_NOT_HANDLED) {
                        logger.error("ACK received but not handled properly - handler issue");
                    }
                }
                //TODO: Consider stopping keep-alive sender or implementing fallback logic
                
            } catch(Exception e) {
                logger.error("Exception in keep-alive timer: ", e);
            }
        }, 5, 30, TimeUnit.SECONDS);
        logger.info("Timer for sending keep Alive packets created!");

        // Schedules overdue packet check every 20 seconds
        timerScheduler.scheduleAtFixedRate(() -> {
            try{
                nodeConnectionManager.checkExpiredConnections();
            } catch (Exception e){
                logger.error("Exception in expiry timer: ", e);
            }
        }, 20, 20, TimeUnit.SECONDS);
        logger.info("Timer for checking Expired connections created!");
    }

    public static void main(String[] args) {

        String instanceId = args.length > 0 ? args[0] : null;


        if(instanceId != "DEFAULT_"+TierRole.SERVER) {
            logger.info("Starting {} Instance with ID: {}", TierRole.SERVER, instanceId);
        } else if(instanceId == null){
            throw new NullPointerException("Cannot have a null command-line arg!");
        } else {        
            logger.warn("Starting with default tier config! ---- DEFAULT_{}",TierRole.SERVER);
            logger.warn("Only one instance of Tier.{} can be made with default config!", TierRole.SERVER);
        }
        
        instanceDTO = new ServerIdentity(TierRole.SERVER, TierRole.COORDINATOR, instanceId);

        try{
            config = new ServerConfig(instanceDTO);
        } catch(Exception e) {
            logger.error("Could not open the {} config!" + e.getMessage(),TierRole.SERVER);
        }

        init();      

        Thread coordinatorThread = new Thread(coordinatorListener); 
        coordinatorThread.start();

        Thread serverThread = new Thread(nodeListener);
        serverThread.start();

        // DEMO
        /*
        Scanner in = new Scanner(System.in);
        boolean on = true;
        while (on) {
            try{
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                logger.error("Main thread interrrupted!\n" + e);
            }

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

                // Send message to the Node
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
                            new ServerConnectionDtoManager(
                                nodeConnectionManager.getConnectionInfoById(nodes.get(nodeNum))
                            ).send(messagePacket);
                            hasNum = true;
                        }
                        else if(trys > 2){
                            System.out.println("Try again next time loser!");
                            hasNum = true;
                        }
                        trys++;
                    }
                } else if(recipient.equals("coordinator") || recipient.equals("Coordinator")) {
                    new ServerConnectionDtoManager(
                        coordinatorConnectionManager.getConnectionInfoById("1"))
                        .send(messagePacket);
                }
                else {
                    System.out.println("Unknown Recipient! \nYour input was: " + recipient + "\nTry again.");
                }
            }
            // Clear message to prevent while loop running again
            message = "";
        }
        in.close();
    */
    }
}