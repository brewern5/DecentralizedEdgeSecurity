/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the main handling point Packets recieved from any node.
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

import components.server.connections.ServerNodeConnectionManager;

import core.exception.NonDelimitedPacket;

import core.external.*;

import core.identity.AbstractTierIdentity;
import core.identity.RuntimeMembershipState;
import core.packet.AbstractPacket;
import core.packet.AbstractPacketManager;
import core.packet.PacketManagerFactory;
import core.packet.PacketType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerNodeHandler implements Runnable {
 
    private static final Logger logger = LogManager.getLogger(ServerNodeHandler.class);

    private static ServerNodeConnectionManager nodeConnectionManager = ServerNodeConnectionManager.getInstance();

    private final AbstractTierIdentity identity;

    private volatile RuntimeMembershipState membershipState;

    private Socket nodeSocket;
    private String nodeIP;

    private AbstractPacket nodePacket;
    private AbstractPacketManager nodePacketManager;

    private BufferedReader reader;  

    private AbstractPacket responsePacket;
 
    public ServerNodeHandler(Socket socket, AbstractTierIdentity identity, RuntimeMembershipState membershipState) {
        this.nodeSocket = socket;
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
                nodeSocket.getOutputStream(), 
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
            "Node connected: \n\t"
            + nodeSocket.getInetAddress().toString()
            + ":" 
            + nodeSocket.getPort()
        );

        try {

            reader = new BufferedReader(
                new InputStreamReader(
                    nodeSocket.getInputStream()
                )
            );

            String payload = reader.readLine();

            if(payload.endsWith("||END||")){
                payload = payload.substring(0, payload.length() - "||END||".length());
            }
            else{
                throw new NonDelimitedPacket("Recieved Packet does not end with \" ||END|| \".");
            }

            String json = payload;

            if (json != null) {

                nodeIP = nodeSocket.getInetAddress().toString();
                nodeIP = nodeIP.substring(1);

                try {
                    Gson gson = new GsonBuilder()
                        .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
                        .create();

                    nodePacket = gson.fromJson(json, AbstractPacket.class);

                    if (!PacketManagerFactory.requiresManager(nodePacket.getPacketType())) {
                        logger.warn("Received response packet type: " + nodePacket.getPacketType());
                        return;
                    }

                    if (nodePacket.getPacketType() == PacketType.INITIALIZATION) {
                        nodePacketManager = PacketManagerFactory.createManager(
                            nodePacket,
                            membershipState,
                            identity.getRole(),
                            nodeConnectionManager,
                            nodeSocket.getInetAddress().getHostAddress()
                        );
                    } else {
                        nodePacketManager = PacketManagerFactory.createManager(
                            nodePacket,
                            membershipState,
                            identity.getRole() 
                        ); 
                    }

                    responsePacket = nodePacketManager.processIncomingPacket();
                    respond();

                } catch(IllegalArgumentException e) {
                    logger.error("Recieved unknown packet");
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch(NonDelimitedPacket e) {
            /* TODO: Create failure sender logic

                */
               logger.error("NonDelimitedPacket!" + e);
        }catch(Exception e) {
            logger.error("Unchecked Exception!" + e);
        } finally {
            try {
                if( reader != null){ reader.close(); }
                if( nodeSocket != null && !nodeSocket.isClosed()) { nodeSocket.close(); }
            } catch (IOException e) {
                logger.error("Error closing socket!\n" + e);
            }
        }
    }
}