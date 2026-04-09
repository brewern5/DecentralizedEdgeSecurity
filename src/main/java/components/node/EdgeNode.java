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
package components.node;

import java.net.SocketException;
import java.net.UnknownHostException;

import java.util.LinkedHashMap;

// DEMO
/*
import java.util.Scanner;
*/

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import components.node.connections.*;
import components.node.identity.NodeIdentity;
import components.node.listener.NodeListener;
import components.node.config.NodeConfig;

import core.connection.ConnectionDto;
import core.connection.Priority;
import core.identity.AbstractTierIdentity;
import core.identity.TierRole;
import core.packet.AbstractPacket;
import core.packet.initalization.InitalizationPacketManager;
import core.config.AbstractConfig;

public class EdgeNode {

    private static volatile String nodeId = null;
    private static volatile String clusterId = null;

    private static volatile AbstractTierIdentity instanceDTO;

    private static String IP;

    private static NodeListener serverListener;            // The socket that will be listening to requests from the Edge server.
    private static NodeListener peerListener;// TODO: Implement P2P comms

    // Each class can have its own logger instance
    private static final Logger logger = LogManager.getLogger(EdgeNode.class);

    private static NodeServerConnectionManager serverConnectionManager; // Manage the connection between the server and the node

    private static NodeServerConnectionManager peerConnectionManager; 
    // Timer components
    private static ScheduledExecutorService timerScheduler; //the timer that will send out the keepAlives to server

    private static AbstractConfig config;



    public static void init() {

        // try/catch to generate the IP from ../../core/config/Config.java - Throws UnknownHostException if it cannot determine the IP
        try{
            IP = config.grabIP();
            logger.info("Starting Node at " + IP);
        } catch (UnknownHostException e) {
            logger.error("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        } catch (SocketException e){
            logger.error("Error: Unable to determine IP Address");
        }

        // Try to connect to the server
        try {
            serverConnectionManager = NodeServerConnectionManager.getInstance("", "", "Node");

            serverConnectionManager.addConnection(
                new ConnectionDto(
                    "1",
                    config.getIPByKey("Server.IP"),
                    config.getPortByKey("Server.listeningPort"),
                    Priority.CRITICAL
                )
            );

            // Create and store payload for INITALIZATION packet
            LinkedHashMap<String, String> payload = new LinkedHashMap<>();
            payload.put(
                "Node.listeningPort", 
                String.valueOf(config.getPortByKey("Node.listeningPort"))
            );

            AbstractPacket initPacket = new InitalizationPacketManager(
                "", 
                "", 
                "1", 
                "Node", serverConnectionManager, 
                IP
            )
            .createOutgoingPacket();

            initPacket.addPayload(payload);

            serverConnectionManager.sendToConnection("1", initPacket);

            setNodeId(serverConnectionManager.getInstanceId());
            setClusterId(serverConnectionManager.getClusterId());

        } catch(Exception e){
            logger.error("Error Sending Initalization Packet: " + e);
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
                + e
            );
            // TODO: try to grab new port if this one is unavailable
        }
        /*
         *      Try and Create timers
         */
        try {
            initializeTimers();
        } catch (Exception e) {
            logger.error("Error creating Timers: \n" + e);
        }
    }
    /*
     *      ID assignment 
     */
    // Thread-safe setter for ID assignment
    public static synchronized void setNodeId(String id) {
        nodeId = id;
        logger.info("Node ID assigned: " + id);
    }

    public static synchronized String getNodeId() {
        return nodeId;
    }

    public static synchronized void setClusterId(String newClusterId) {
        clusterId = newClusterId;
        logger.info("Cluster ID assigned: " + newClusterId);
    }

    public static synchronized String getClusterId() {
        return clusterId;
    }

    // Peer list request 
    public static synchronized void peerListReq() {

        // TODO:
        /*         // Try and and get a peer list from the connected server
        try {
            boolean peerListReqSent;

            
            peerListReqSent = serverConnectionManager.sendPeerListReq(
                NodePeerListService.createPeerListReq(
                    getNodeId(), 
                    IP, 
                    getClusterId()
                )
            );

            if(!peerListReqSent) {

            }
        } catch(Exception e) {
            logger.error("Could not get Peer List: " + e);
        }*/
    }

    /*
     *      Timer creation
     */
    private static void initializeTimers() {
        // Creates the timer for 
        timerScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "EdgeNode-Timer");
            //t.setDaemon(true);
            return t;
        });
        // Schedules the sending for the keepAlive packet every 30 seconds
        timerScheduler.scheduleAtFixedRate(() -> {
            try {
                boolean keepAliveSent;

                keepAliveSent = serverConnectionManager.sendKeepAlive();

                if(!keepAliveSent){
                    logger.error("Keep alive was not sent!");
                }

            } catch(core.exception.KeepAliveException e) {
                // Detailed logging for keep-alive specific failures
                logger.error("Keep-alive failed at stage: {} for connection: {} - {}", 
                           e.getStage(), e.getConnectionId(), e.getMessage());
                
                // Different handling based on failure type
                if (e.isSendFailure()) {
                    logger.error("Failed to send keep-alive packet - network or socket issue");
                } else if (e.isAckFailure()) {
                    if (e.getStage() == core.exception.KeepAliveException.FailureStage.ACK_NOT_RECEIVED) {
                        logger.error("Keep-alive sent but ACK not received - "+instanceDTO.getHigherTier()+" may be down");
                    } else if (e.getStage() == core.exception.KeepAliveException.FailureStage.ACK_NOT_HANDLED) {
                        logger.error("ACK received but not handled properly - handler issue");
                    }
                }
            } catch(Exception e) {
                logger.error("Exception in keep-alive timer: ", e);
            }
        }, 5, 30, TimeUnit.SECONDS);
    }

    /*
     *      Main Loop
     */

    public static void main(String[] args) {

        // Create instance ID through command-line args
        String instanceId = args.length > 0 ? args[0] : null; // When starting the server arguments depicting an instance number (i.e. server1, server2)
    
        
        if(instanceId != "DEFAULT_"+TierRole.NODE) {
            logger.info("Starting {} Instance with ID: {}", TierRole.NODE, instanceId);
        } else if(instanceId == null){
            throw new NullPointerException("Cannot have a null command-line arg!");
        } else {        
            logger.warn("Starting with default tier config! ---- DEFAULT_{}", TierRole.NODE);
            logger.warn("Only one instance of Tier.{} can be made with default config!", TierRole.NODE);
        }

        instanceDTO = new NodeIdentity(TierRole.NODE, TierRole.SERVER, instanceId);

        try{
            config = new NodeConfig(instanceDTO);
        } catch(Exception e) {
            logger.error("Could not open the {} config!" + e.getMessage(), TierRole.NODE);
        }
        

        init();         // Begins the initalization process 

        Thread serverThread = new Thread(serverListener);
        serverThread.start();

        // Request the peer list   
        peerListReq();

        // DEMO
        /*
        Scanner in = new Scanner(System.in);
        
        boolean on = true;
        while(on){
            
        System.out.println("Manually Send Message to Server: ");
        String message = in.nextLine();
        
        if(!message.isEmpty()) {
                NodePacket messagePacket = new NodeGenericPacket(
                    NodePacketType.MESSAGE,
                    getNodeID(),
                    message
                );
                
                new NodeConnectionDtoManager(serverConnectionManager.getConnectionInfoById("1")).send(messagePacket);
            }
        }       
        in.close();
        */
    }
}
