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

package server.server_handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_connections.ServerNodeConnectionManager;

import packet.AbstractPacket;
import packet.AbstractPacketManager;
import packet.PacketManagerFactory;
import packet.PacketType;

import external.*;

import exception.NonDelimitedPacket;

public class ServerNodeHandler implements Runnable {
 
    private static final Logger logger = LogManager.getLogger(ServerNodeHandler.class);

    private static ServerNodeConnectionManager nodeConnectionManager = ServerNodeConnectionManager.getInstance();

    private Socket nodeSocket;
    private String nodeIP;

    private AbstractPacket nodePacket;
    private AbstractPacketManager nodePacketManager;

    private BufferedReader reader;  

    // Packet designed to be sent back to the initial sender, generic type so the type will need to be specified on instantiation
    private AbstractPacket responsePacket;
 
    public ServerNodeHandler(Socket socket) {
        this.nodeSocket = socket;
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
                nodeSocket.getOutputStream(), 
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
            "Node connected: \n\t"
            + nodeSocket.getInetAddress().toString()
            + ":" 
            + nodeSocket.getPort()
        );

        // Handle client events
        try {

            // This is what decodes the incoming packet
            reader = new BufferedReader(
                new InputStreamReader(
                    nodeSocket.getInputStream()
                )
            );

            // Stores the payload as a string to check (and potentially remove) the delimiter
            String payload = reader.readLine();

            // Checks if the payload is properly terminated. If not, the packet is incomplete or an unsafe packet was sent
            if(payload.endsWith("||END||")){
                payload = payload.substring(0, payload.length() - "||END||".length());
            }
            else{
                throw new NonDelimitedPacket("Recieved Packet does not end with \" ||END|| \".");
            }

            // Reads the packet as json
            String json = payload;

            // Checks if empty packet
            if (json != null) {

                // Grabs the server IP in order to be saved in config file
                nodeIP = nodeSocket.getInetAddress().toString();
                nodeIP = nodeIP.substring(1); // Removes the forward slash

                try {
                    // Set's up the Factory to be able to reconstruct the packet to it's correct class
                    Gson gson = new GsonBuilder()
                        .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
                        .create();

                    // Reconstructs the packet to it's desired type
                    nodePacket = gson.fromJson(json, AbstractPacket.class);

                    // Check if packet type needs a manager
                    if (!PacketManagerFactory.requiresManager(nodePacket.getPacketType())) {
                        logger.warn("Received response packet type: " + nodePacket.getPacketType());
                        return; // Response packets don't need managers
                    }

                    // Initialization packets need to be handled differently
                    if (nodePacket.getPacketType() == PacketType.INITIALIZATION) {
                        nodePacketManager = PacketManagerFactory.createManager(
                            nodePacket,
                            nodeConnectionManager.getInstanceId(),
                            nodeConnectionManager.getClusterId(),
                            "Server",
                            nodeConnectionManager,
                            nodeSocket.getInetAddress().getHostAddress()  // From socket - convert to String
                        );
                    } else {
                        nodePacketManager = PacketManagerFactory.createManager(
                            nodePacket,
                            nodeConnectionManager.getInstanceId(),
                            nodeConnectionManager.getClusterId(),
                            "Server" 
                        ); 
                    }

                    responsePacket = nodePacketManager.processIncomingPacket();
                    respond();

                } catch(IllegalArgumentException e) {
                    logger.error("Recieved unknown packet");
                    return; // Early exit
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