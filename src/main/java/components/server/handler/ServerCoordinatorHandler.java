/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the main handling point Packets recieved from the Coordinator.
 *      All recieved packets will be sent from it's respective thread to
 *      here where the packet will be checked in this order:
 *          - Proper termination
 *              - Will respond with a failure packet if delimiter is not at the end
 *          - Packet Type 
 *              - Sends to a switch case with the different packet types which will in
 *                turn be handled differently dependent on the type. Each packet type
 *                handler will be in their own class
 * 
 *      Once the packet has been handled, the repective Packet Type handler will have 
 *      created a 'HandleResponse' object, that will be the response messages, exceptions
 *      (If there are exceptions) and the success status(Boolean). If the success status
 *      is true, then a ACK packet will be sent, else a Failure packet or Error packet will
 *      be sent instead.
 */
package components.server.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import components.server.connections.ServerCoordinatorConnectionManager;

import core.exception.NonDelimitedPacket;

import core.external.PacketTypeAdapterFactory;

import core.packet.AbstractPacket;
import core.packet.AbstractPacketManager;
import core.packet.PacketManagerFactory;
import core.packet.PacketType;

import core.identity.AbstractTierIdentity;
import core.identity.RuntimeMembershipState;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerCoordinatorHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(ServerCoordinatorHandler.class);

    private Socket coordinatorSocket;
    private String coordinatorIP;

    private BufferedReader reader;

    private AbstractPacket coordinatorPacket;
    private AbstractPacketManager coordinatorPacketManager;

    private ServerCoordinatorConnectionManager coordinatorConnectionManager = ServerCoordinatorConnectionManager.getInstance();

    private AbstractPacket responsePacket;

    private final AbstractTierIdentity identity;

    private volatile RuntimeMembershipState membershipState;

    public ServerCoordinatorHandler(Socket socket, AbstractTierIdentity identity, RuntimeMembershipState membershipState) {
        this.coordinatorSocket = socket;
        this.identity = identity;
        this.membershipState = membershipState;
    }

    /*
     *          Respond
     */

    private void respond() {

        String json = responsePacket.toDelimitedString();

        try{
            PrintWriter output = new PrintWriter(
                coordinatorSocket.getOutputStream(), 
                true
            );
            output.println(json);
        } catch (IOException e) {
            logger.error("Error sending response packet of type: " + responsePacket.getPacketType() + "\n", e);
        }
    }
    /*
     *                      Main run loop
     */
    @Override
    public void run() {

        logger.info(
            "Coordinator connected: \n\t" 
            + coordinatorSocket.getInetAddress().toString() 
            + ":"
            + coordinatorSocket.getPort()
        );

        try{
            reader = new BufferedReader(
                new InputStreamReader(
                    coordinatorSocket.getInputStream()
                )
            );
            
            String jsonPacket = reader.readLine();

            if(jsonPacket.endsWith("||END||")){
                jsonPacket = jsonPacket.substring(
                    0, 
                    jsonPacket.length() - "||END||".length()
                );
            }
            else{
                throw new NonDelimitedPacket("Recieved Packet does not end with \" ||END|| \".");
            }
            String json = jsonPacket;

            if (json != null) {    
                
                coordinatorIP = coordinatorSocket.getInetAddress().toString();
                coordinatorIP = coordinatorIP.substring(1);

                try {
                    Gson gson = new GsonBuilder()
                        .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
                        .create();

                    coordinatorPacket = gson.fromJson(json, AbstractPacket.class);

                    if (!PacketManagerFactory.requiresManager(coordinatorPacket.getPacketType())) {
                        if (coordinatorPacket.getPacketType() == PacketType.INITIALIZATION_RES) {
                            membershipState.assignId(coordinatorPacket.getRecipientId());
                            membershipState.assignClusterId(coordinatorPacket.getClusterId());
                            logger.info(
                                "Runtime membership updated from INITIALIZATION_RES: assignedId={}, clusterId={}",
                                membershipState.assignedId(),
                                membershipState.clusterId()
                            );
                        }
                        logger.warn("Received response packet type: " + coordinatorPacket.getPacketType());
                        return;
                    }

                    if (coordinatorPacket.getPacketType() == PacketType.INITIALIZATION) {
                        coordinatorPacketManager = PacketManagerFactory.createManager(
                            coordinatorPacket,
                            membershipState,
                            identity.getRole(),
                            coordinatorConnectionManager,
                            coordinatorSocket.getInetAddress().getHostAddress() 
                        );
                    } else {
                        coordinatorPacketManager = PacketManagerFactory.createManager(
                            coordinatorPacket,
                            membershipState,
                            identity.getRole()
                        ); 
                    } 

                    responsePacket = coordinatorPacketManager.processIncomingPacket();
                    respond();

                } catch(IllegalArgumentException e) {
                    logger.error("Recieved unknown packet!");
                    return; 
                }
            }
        } catch (IOException e) {
             logger.error("I/O Error! " + e);
        } catch(NonDelimitedPacket e) {
            /* TODO: Create failure sender logic
 
                */
               logger.error("NonDelimitedPacket!" + e);
        } catch(Exception e) {
            logger.error("Unchecked Exception!" + e);
        } finally {
            try {
                if( reader != null){ reader.close(); }
                if( coordinatorSocket != null && !coordinatorSocket.isClosed()) { coordinatorSocket.close(); }
                logger.info("Closing handler thread.");
            } catch (IOException e) {
                logger.error("Error closing socket!" + e);
            }
        }
    }
}