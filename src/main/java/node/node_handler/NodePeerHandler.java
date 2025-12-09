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

package node.node_handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import external.PacketTypeAdapterFactory;
import external.RuntimeTypeAdapterFactory;

import exception.NonDelimitedPacket;

import node.node_connections.NodePeerConnectionManager;

import packet.AbstractPacket;
import packet.AbstractPacketManager;
import packet.PacketManagerFactory;
import packet.PacketType;

public class NodePeerHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(NodePeerHandler.class);

    private Socket peerSocket;
    private String peerIP;

    private AbstractPacket peerPacket;
    private AbstractPacketManager peerPacketManager;

    private BufferedReader reader;
   
    private NodePeerConnectionManager peerConnectionManager = NodePeerConnectionManager.getInstance();

    // Packet designed to be sent back to the initial sender, generic type so the type will need to be specified on instantiation
    private AbstractPacket responsePacket;

    public NodePeerHandler(Socket socket) {
        this.peerSocket = socket;
    }

    /*          
     *          Respond
     */

    // Takes an already initalized response packet and returns to sender
    private void respond() {

        // Puts the contents of the packet to JSON with a non-JSON compatable delimiter at the end to be handled prior to pakcet content hanlding
        String json = responsePacket.toDelimitedString();

        try{
            // The responder object
            PrintWriter output = new PrintWriter(
                peerSocket.getOutputStream(), 
                true
            );
            // Send the jsonified packet as a response
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

        // Handle client events
        try {
            // This is what decodes the incoming packet
            reader = new BufferedReader(
                new InputStreamReader(
                    peerSocket.getInputStream()
                )
            );

            // Stores the payload as a string to check (and potentially remove) the delimiter
            String jsonPacket = reader.readLine();

            // Checks if the payload is properly terminated. If not, the packet is incomplete or an unsafe packet was sent
            if(jsonPacket.endsWith("||END||")){
                jsonPacket = jsonPacket.substring(
                    0, 
                    jsonPacket.length() - "||END||".length()
                );
            }
            else{
                throw new NonDelimitedPacket("Recieved Packet does not end with \" ||END|| \".");
            }

            // Reads the packet as json
            String json = jsonPacket;

            // Checks if empty packet
            if (json != null) {

                // Grabs the server IP in order to be saved in config file
                peerIP = peerSocket.getInetAddress().toString();
                peerIP = peerIP.substring(1); // Removes the forward slash

                try {
                   // Set's up the Factory to be able to reconstruct the packet to it's correct class
                    Gson gson = new GsonBuilder()
                        .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
                        .create();

                    // Reconstructs the packet to it's desired type
                    peerPacket = gson.fromJson(json, AbstractPacket.class);

                    // Check if packet type needs a manager
                    if (!PacketManagerFactory.requiresManager(peerPacket.getPacketType())) {
                        logger.warn("Received response packet type: " + peerPacket.getPacketType());
                        return; // Response packets don't need managers
                    }
                        peerPacketManager = PacketManagerFactory.createManager(
                            peerPacket,
                            peerConnectionManager.getInstanceId(),
                            peerConnectionManager.getClusterId(),
                            "Node"
                        ); 

                } catch(IllegalArgumentException e) {
                    logger.error("Recieved unknown packet!" + e);
                    return; // Early exit
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