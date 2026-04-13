/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the main handling point Packets recieved from the server.
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

package components.node.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import components.node.connections.NodePeerConnectionManager;
import components.node.identity.NodeIdentity;
import core.exception.NonDelimitedPacket;
import core.external.PacketTypeAdapterFactory;
import core.external.RuntimeTypeAdapterFactory;
import core.identity.RuntimeMembershipState;
import core.packet.AbstractPacket;
import core.packet.AbstractPacketManager;
import core.packet.PacketManagerFactory;
import core.packet.PacketType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NodePeerHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(NodePeerHandler.class);

    private Socket peerSocket;
    private String peerIP;

    private AbstractPacket peerPacket;
    private AbstractPacketManager peerPacketManager;

    private BufferedReader reader;
   
    private NodePeerConnectionManager peerConnectionManager = NodePeerConnectionManager.getInstance();

    private AbstractPacket responsePacket;

    private final NodeIdentity identity;
    private final RuntimeMembershipState membershipState;

    public NodePeerHandler(Socket socket, NodeIdentity identity, RuntimeMembershipState membershipState) {
        this.peerSocket = socket;
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
                peerSocket.getOutputStream(), 
                true
            );
            output.println(json);
            output.close();
        } catch (IOException e) {
            logger.error("Error sending response packet of type: " + responsePacket.getPacketType() + "\n"+ e);
        }
    }
    /*
     *                      Main run loop
     */

    @Override
    public void run() {
        
        logger.info(
            "Peer connected: \n\t"
            + peerSocket.getInetAddress().toString()
            + ":" 
            + peerSocket.getPort()
        );

        try {
            reader = new BufferedReader(
                new InputStreamReader(
                    peerSocket.getInputStream()
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

                peerIP = peerSocket.getInetAddress().toString();
                peerIP = peerIP.substring(1);

                try {
                    Gson gson = new GsonBuilder()
                        .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
                        .create();

                    peerPacket = gson.fromJson(json, AbstractPacket.class);

                    if (!PacketManagerFactory.requiresManager(peerPacket.getPacketType())) {
                        logger.warn("Received response packet type: " + peerPacket.getPacketType());
                        return; 
                    }
                    if (peerPacket.getPacketType() == PacketType.INITIALIZATION) {
                        peerPacketManager = PacketManagerFactory.createManager(
                            peerPacket,
                            membershipState,
                            identity.getRole(),
                            peerConnectionManager,
                            peerSocket.getInetAddress().getHostAddress()
                        );
                    } else {
                        peerPacketManager = PacketManagerFactory.createManager(
                            peerPacket,
                            membershipState,
                            identity.getRole()
                        );
                    }

                } catch(IllegalArgumentException e) {
                    logger.error("Recieved unknown packet!" + e);
                    return;
                }
            }
        } catch(NonDelimitedPacket e) {
            /* TODO: Create failure sender logic
 
                */
            logger.error("NonDelimitedPacket!" + e);
        } catch (IOException e) {
            logger.error("I/O exception!" + e);
        } finally {
            try {
                if( reader != null){ reader.close(); }
                if( peerSocket != null && !peerSocket.isClosed()) { peerSocket.close(); }
            } catch (IOException e) {
                logger.error("Error closing socket!\n" + e);
            }
        }
    }
}