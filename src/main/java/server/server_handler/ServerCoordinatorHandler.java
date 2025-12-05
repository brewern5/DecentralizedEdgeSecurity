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
package server.server_handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import exception.NonDelimitedPacket;
import external.PacketTypeAdapterFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_connections.ServerCoordinatorConnectionManager;

import packet.AbstractPacket;
import packet.AbstractPacketManager;
import packet.PacketManagerFactory;
import packet.PacketType;

public class ServerCoordinatorHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(ServerCoordinatorHandler.class);

    private Socket coordinatorSocket;
    private String coordinatorIP;

    private BufferedReader reader;

    private AbstractPacket coordinatorPacket;
    private AbstractPacketManager coordinatorPacketManager;

    private ServerCoordinatorConnectionManager coordinatorConnectionManager = ServerCoordinatorConnectionManager.getInstance();


    // Packet designed to be sent back to the initial sender, generic type so the type will need to be specified on instantiation
    private AbstractPacket responsePacket;

    // Constructor
    public ServerCoordinatorHandler(Socket socket) {
        this.coordinatorSocket = socket;
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
                coordinatorSocket.getOutputStream(), 
                true
            );
            // Send the jsonified packet as a response
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

        // Handle client events
        try{
            // This is what decodes the incoming packet
            reader = new BufferedReader(
                new InputStreamReader(
                    coordinatorSocket.getInputStream()
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
                coordinatorIP = coordinatorSocket.getInetAddress().toString();
                coordinatorIP = coordinatorIP.substring(1); // Removes the forward slash

                try {
                    // Set's up the Factory to be able to reconstruct the packet to it's correct class
                    Gson gson = new GsonBuilder()
                        .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
                        .create();

                    // Reconstructs the packet to it's desired type
                    coordinatorPacket = gson.fromJson(json, AbstractPacket.class);

                    // Check if packet type needs a manager
                    if (!PacketManagerFactory.requiresManager(coordinatorPacket.getPacketType())) {
                        logger.warn("Received response packet type: " + coordinatorPacket.getPacketType());
                        return; // Response packets don't need managers
                    }

                    // Initialization packets need to be handled differently
                    if (coordinatorPacket.getPacketType() == PacketType.INITIALIZATION) {
                        coordinatorPacketManager = PacketManagerFactory.createManager(
                            coordinatorPacket,
                            coordinatorConnectionManager.getInstanceId(),
                            coordinatorConnectionManager.getClusterId(),
                            "Server",
                            coordinatorConnectionManager,
                            coordinatorSocket.getInetAddress().getHostAddress()  // From socket - convert to String
                        );
                    } else {
                        coordinatorPacketManager = PacketManagerFactory.createManager(
                            coordinatorPacket,
                            coordinatorConnectionManager.getInstanceId(),
                            coordinatorConnectionManager.getClusterId(),
                            "Server"
                        ); 
                    } 

                    responsePacket = coordinatorPacketManager.processIncomingPacket();
                    respond();

                } catch(IllegalArgumentException e) {
                    logger.error("Recieved unknown packet!");
                    return; // Early exit
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