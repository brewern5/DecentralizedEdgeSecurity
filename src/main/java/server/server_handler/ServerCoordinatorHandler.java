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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.edge_server.EdgeServer;

import server.server_connections.server_connection_manager.*;
import server.server_handler.server_packet_type_handler.*;

import server.server_lib.RuntimeTypeAdapterFactory;

import server.server_packet.server_packet_class.*;
import server.server_packet.*;

public class ServerCoordinatorHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(ServerCoordinatorHandler.class);

    private Socket coordinatorSocket;
    private String coordinatorIP;

    private BufferedReader reader;

    private ServerConnectionManager coordinatorConnectionManager = ServerCoordinatorConnectionManager.getInstance();

    private ServerPacket coordinatorPacket;

    // Packet designed to be sent back to the initial sender, generic type so the type will need to be specified on instantiation
    private ServerPacket responsePacket;

    // Constructor
    public ServerCoordinatorHandler(Socket socket) {
        this.coordinatorSocket = socket;
    }

    /*
     *          Packet Reconstruction
     */

    private ServerPacket buildGsonWithPacketSubtype(ServerPacketType type, String json) {
        RuntimeTypeAdapterFactory<ServerPacket> packetAdapterFactory =
        RuntimeTypeAdapterFactory
            .of(ServerPacket.class, "packetType")
            .registerSubtype(ServerGenericPacket.class, type.name());

        Gson tempGson = new GsonBuilder()
            .registerTypeAdapterFactory(packetAdapterFactory)
            .create();

        return tempGson.fromJson(json, ServerPacket.class);
    }

    /*     
     *          Responses
     */

    // This is a good response, it will be sent back to the server to ensure a packet was recieved 
    private void ack(ServerHandlerResponse packetResponse) {

        responsePacket = new ServerGenericPacket(
            ServerPacketType.ACK,       // Packet type
            EdgeServer.getServerId(),            // Sender
            packetResponse.combineMaps()   // Payload
        );
        respond();
    }

    // This is the creation of the failure packet based on the packet response 
    private void failure(ServerHandlerResponse packetResponse) {
        
        // Construct a new failure packet
        responsePacket = new ServerGenericPacket(
            ServerPacketType.ERROR,    // Packet type
            EdgeServer.getServerId(),  // Sender
            packetResponse.combineMaps()  // Payload
        );
        // Send the packet
        respond();
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
            String payload = reader.readLine();

            // Checks if the payload is properly terminated. If not, the packet is incomplete or an unsafe packet was sent
            if(payload.endsWith("||END||")){
                payload = payload.substring(
                    0, 
                    payload.length() - "||END||".length()
                );
            }
            else{
                failure(
                    new ServerHandlerResponse(
                        false, 
                        new Exception("Payload not terminated."), 
                        "Incomplete Packet"
                    )
                );
            }
            // Reads the packet as json
            String json = payload;

            // Checks if empty packet
            if (json != null) {
                ServerPacketHandler packetHandler;    // This will will be instantiated based on the PacketType that needs to handle this. I.e initalizationHandler

                ServerHandlerResponse packetResponse; // The response from the handling of the packet.    
                
                // Grabs the server IP in order to be saved in config file
                coordinatorIP = coordinatorSocket.getInetAddress().toString();
                coordinatorIP = coordinatorIP.substring(1); // Removes the forward slash

                // Pre-parses the Json in order to grab the packetType to use for the switch statement
                JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();

                String packetTypeStr = jsonObj.get("packetType").getAsString();

                // Covert the string to the ENUM packetType
                ServerPacketType packetType = ServerPacketType.valueOf(packetTypeStr);

                // Checks the packet type to determine how it needs to handle it
                switch (packetType) {
                    case AUTH:
                        // TODO: Handle authentication logic
                        break;
                    case MESSAGE:
                        // Builds the packet to be handled
                        coordinatorPacket = buildGsonWithPacketSubtype(packetType, json);

                        logger.info("Recieved:" + coordinatorPacket.toJson());

                        // Create the packetType based handler (true = use CoordinatorConnectionManager)
                        packetHandler = new ServerMessageHandler(true);

                        // Allow for the packet response to be created based on the handling response
                        packetResponse = packetHandler.handle(coordinatorPacket); 

                        // If good send ack
                        if(packetResponse.getSuccess() == true){
                            // Construct the ack packet
                            ack(packetResponse);
                        }
                        else if(packetResponse.getSuccess() == false){
                            logger.error("Error Handling Packet of Type: \t MESSAGE \n\t Details:");

                            // Detail the errors
                            packetResponse.printMessages();
                          
                            // Construct the failure packet based on the response
                            failure(packetResponse);
                        }
                        break;
                    case KEEP_ALIVE:
                        // Builds the packet to be handled
                        coordinatorPacket = buildGsonWithPacketSubtype(packetType, json);

                        logger.info("Recieved:\n\t" + coordinatorPacket.toJson());

                        // Create the packetType based handler (true = use CoordinatorConnectionManager)
                        packetHandler = new ServerKeepAliveHandler(true);

                        // Allow for the packet response to be created based on the handling response
                        packetResponse = packetHandler.handle(coordinatorPacket); 

                        // If good send ack
                        if(packetResponse.getSuccess() == true){
                            // Construct the ack packet
                            ack(packetResponse);
                        }
                        else if(packetResponse.getSuccess() == false){
                            logger.error("Error Handling Packet of Type: \t MESSAGE \n\t Details:");
                            // Detail the errors
                            packetResponse.printMessages();
                          
                            // Construct the failure packet based on the response
                            failure(packetResponse);
                        }
                        break;
                    default:
                        // TODO: Handle unknown or unsupported packet types
                        break;
                }
            }
        } catch (IOException e) {
             logger.error("I/O Error! " + e);
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