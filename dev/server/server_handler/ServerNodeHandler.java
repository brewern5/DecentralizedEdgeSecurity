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

import server_config.ServerConfig;
import server_handler.server_packet_handler.ServerHandlerResponse;
import server_handler.server_packet_handler.ServerInitalizationHandler;
import server_handler.server_packet_handler.ServerPacketHandler;
import server_packet.ServerPacket;
import server_packet.ServerPacketType;

public class ServerNodeHandler implements Runnable {

    private Socket nodeSocket;
    private String nodeIP;

    private ServerConfig config = new ServerConfig();

    private ServerPacket nodePacket;

    // Packet designed to be sent back to the initial sender, generic type so the type will need to be specified on instantiation
    private ServerPacket responsePacket;

    public ServerNodeHandler(Socket socket) {
        this.nodeSocket = socket;
    }

    // This is a good response, it will be sent back to the server to ensure a packet was recieved 
    private void ack(ServerHandlerResponse packetResponse) {

        responsePacket = new ServerPacket(
                ServerPacketType.ACK,        // Packet type
                "Server",             // Sender
                packetResponse.toString()    // Payload
        );
        respond();
    }

    // This is the creation of the failure packet based on the packet response 
    private void failure(ServerHandlerResponse packetResponse) {
        
        // Construct a new failure packet
        responsePacket = new ServerPacket(
                ServerPacketType.ERROR,            // Packet type
                "Server",                   // Sender
                packetResponse.toString() // Payload
        );
        // Send the packet
        respond();
    }

    /*
     *                     Acknowledgment
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

        Gson gson = new Gson();     // Allows us to decode the json string from the message
        
        System.out.println(
            "Node connected from "
            + nodeSocket.getInetAddress().toString()
            + ":" 
            + nodeSocket.getPort()
        );

        // Handle client events
        try {

            // This is what decodes the incoming packet
            BufferedReader reader = new BufferedReader(
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
                
                // Instantiates a new packet with the json 
                nodePacket = gson.fromJson(json, ServerPacket.class);

                // Checks the packet type to determine how it needs to handle it
                switch (nodePacket.getPacketType()) {
                    case INITIALIZATION:

                        System.out.println("\nRecieved:\n\n" + nodePacket.toString() + "\n\n");

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
                        // TODO: Handle message logic
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
            reader.close();

            // TODO: check some other things before closing the sockets
            nodeSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}