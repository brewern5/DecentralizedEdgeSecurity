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

package server_handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import server_handler.server_packet_type_handler.*;
import server_lib.RuntimeTypeAdapterFactory;
import server_packet.ServerPacket;
import server_packet.ServerPacketType;
import server_packet.server_packet_class.*;

public class ServerNodeHandler implements Runnable {

    private Socket nodeSocket;
    private String nodeIP;

    private ServerPacket nodePacket;

    private BufferedReader reader;

    // Packet designed to be sent back to the initial sender, generic type so the type will need to be specified on instantiation
    private ServerPacket responsePacket;

    public ServerNodeHandler(Socket socket) {
        this.nodeSocket = socket;
    }

    /*
     *  
     *          Packet Reconstruction
     * 
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

    /*          End Packet Reconstruction
     * 
     * 
     *          Responses
     * 
     */

    // This is a good response, it will be sent back to the server to ensure a packet was recieved 
    private void ack(ServerHandlerResponse packetResponse) {

        responsePacket = new ServerGenericPacket(
                ServerPacketType.ACK,        // Packet type
                "Server",             // Sender
                packetResponse.combineMaps()    // Payload
        );
        respond();
    }

    // This is the creation of the failure packet based on the packet response 
    private void failure(ServerHandlerResponse packetResponse) {
        
        // Construct a new failure packet
        responsePacket = new ServerGenericPacket(
                ServerPacketType.ERROR,            // Packet type
                "Server",                   // Sender
                packetResponse.combineMaps() // Payload
        );
        // Send the packet
        respond();
    }

    /*          End Responses
     * 
     * 
     *          Respond
     * 
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
            System.err.println("Error sending response packet of type: " + responsePacket.getPacketType());
            e.printStackTrace();
        }
    }
    /*
     *                      Main run loop
     */

    @Override
    public void run() {
        
        System.out.println(
            "Node connected from "
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
                failure(new ServerHandlerResponse(
                    false, 
                    new Exception("Payload not terminated."), 
                    "Incomplete Packet")
                );
                return; // Early exit
            }

            // Reads the packet as json
            String json = payload;

            // Checks if empty packet
            if (json != null) {
                ServerPacketHandler packetHandler;    // This will will be instantiated based on the PacketType that needs to handle this. I.e initalizationHandler

                ServerHandlerResponse packetResponse; // The response from the handling of the packet.    
                
                // Grabs the server IP in order to be saved in config file
                nodeIP = nodeSocket.getInetAddress().toString();
                nodeIP = nodeIP.substring(1); // Removes the forward slash

                // Pre-parses the Json in order to grab the packetType to use for the switch statement
                JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();

                String packetTypeStr = jsonObj.get("packetType").getAsString();

                // Covert the string to the ENUM packetType
                ServerPacketType packetType = ServerPacketType.valueOf(packetTypeStr);

                // Checks the packet type to determine how it needs to handle it
                switch (packetType) {
                    case INITIALIZATION:

                        // Builds the packet to be handled
                        nodePacket = buildGsonWithPacketSubtype(packetType, json);

                        System.out.println("\nRecieved:\n\n" + nodePacket.toJson() + "\n\n");

                        // Create the packetType based handler
                        packetHandler = new ServerInitalizationHandler();

                        // Allow for the packet response to be created based on the handling response
                        packetResponse = packetHandler.handle(nodePacket); 

                        // If good send ack
                        if(packetResponse.getSuccess() == true){

                            // Construct the ack packet
                            ack(packetResponse);
                        }
                        else if(packetResponse.getSuccess() == false){
                            System.err.println("Error Handling Packet of Type: \tINITIALIZATION\n\nDetails:");

                            // Detail the errors
                            packetResponse.printMessages();
                          
                            // Construct the failure packet based on the response
                            failure(packetResponse);
                        }
                        break;
                    case AUTH:
                        // TODO: Handle authentication logic
                        break;
                    case MESSAGE:
                        // Builds the packet to be handled
                        nodePacket = buildGsonWithPacketSubtype(packetType, json);

                        System.out.println("\nRecieved:\n\n" + nodePacket.toJson() + "\n\n");

                        // Create the packetType based handler
                        packetHandler = new ServerMessageHandler();

                        // Allow for the packet response to be created based on the handling response
                        packetResponse = packetHandler.handle(nodePacket); 

                        // If good send ack
                        if(packetResponse.getSuccess() == true){

                            // Construct the ack packet
                            ack(packetResponse);
                        }
                        else if(packetResponse.getSuccess() == false){
                            System.err.println("Error Handling Packet of Type: \t MESSAGE \n\n Details:");

                            // Detail the errors
                            packetResponse.printMessages();
                          
                            // Construct the failure packet based on the response
                            failure(packetResponse);
                        }
                        break;
                    case COMMAND:
                        // TODO: Handle command logic
                        break;
                    case HEARTBEAT:
                        // TODO: Handle heartbeat logic
                        break;
                    case STATUS:
                        // TODO: Handle status logic
                        break;
                    case DATA:             
                        // TODO: Bulk or sensor data
                        break;
                    case ERROR:            
                        // TODO: Error or exception reporting
                        break;
                    case ACK:              
                        // TODO: handle Acknowledgement of receipt
                        break;
                    case DISCONNECT:
                        // TODO: handle disconnect cases
                        break;
                    default:
                        // TODO: Handle unknown or unsupported packet types
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if( reader != null){ reader.close(); }
                if( nodeSocket != null && !nodeSocket.isClosed()) { nodeSocket.close(); }
            } catch (IOException e) {
                System.err.println("Error closing socket!");
                e.printStackTrace();
            }
        }
    }
}