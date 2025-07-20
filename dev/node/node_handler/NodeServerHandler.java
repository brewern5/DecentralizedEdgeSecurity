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
package node_handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import com.google.gson.Gson;

import node_handler.node_packet_handler.NodeHandlerResponse;
import node_handler.node_packet_handler.NodeInitalizationHandler;
import node_handler.node_packet_handler.NodePacketHandler;
import node_packet.NodePacket;
import node_packet.NodePacketType;
import node_packet.node_packet_class.NodeGenericPacket;
import node_config.NodeConfig; 

public class NodeServerHandler implements Runnable {

    private Socket serverSocket;
    private String serverIP;

    private NodePacket serverPacket;

    private NodeConfig config = new NodeConfig();

    // Packet designed to be sent back to the initial sender, generic type so the type will need to be specified on instantiation
    private NodePacket responsePacket;

    // Constructor
    public NodeServerHandler(Socket socket) {
        this.serverSocket = socket;
    }

    // This is a good response, it will be sent back to the server to ensure a packet was recieved 
    private void ack(NodeHandlerResponse packetResponse) {

        responsePacket = new NodeGenericPacket(
            NodePacketType.ACK,         
            "Node",              
            packetResponse.getMessageMap() 
        );
        respond();
    }

    // This is the creation of the failure packet based on the packet response 
    private void failure(NodeHandlerResponse packetResponse) {
        
        // Construct a new failure packet
        responsePacket = new NodeGenericPacket(
            NodePacketType.ERROR,   
            "Node",  
            packetResponse.getMessageMap() 
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
                serverSocket.getOutputStream(), 
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

        Gson gson = new Gson();

        System.out.println(
            "Server connected: " 
            + serverSocket.getInetAddress().toString() 
            + "\n\tFrom port: "
            + serverSocket.getPort()
        );

        // Handle client events
        try{

            // This is what decodes the incoming packet
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    serverSocket.getInputStream()
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
                failure(new NodeHandlerResponse(
                    false, 
                    new Exception("Payload not terminated."), 
                    "Incomplete Packet")
                );
            }


            // Reads the packet as json
            String json = payload;

            // Checks if empty packet
            if (json != null) {
                NodePacketHandler packetHandler;    // This will will be instantiated based on the PacketType that needs to handle this. I.e initalizationHandler

                NodeHandlerResponse packetResponse; // The response from the handling of the packet.    
                
                // Grabs the server IP in order to be saved in config file
                serverIP = serverSocket.getInetAddress().toString();
                serverIP = serverIP.substring(1); // Removes the forward slash
                
                // Instantiates a new packet with the json 
                serverPacket = gson.fromJson(json, NodePacket.class);

                // Checks the packet type to determine how it needs to handle it
                switch (serverPacket.getPacketType()) {
                    case INITIALIZATION:

                        System.out.println(
                            "\nRecieved:\n\n" 
                            + serverPacket.toString() 
                            + "\n\n"
                        );

                        // Create the packetType based handler
                        packetHandler = new NodeInitalizationHandler();

                        // Allow for the packet response to be created based on the handling response
                        packetResponse = packetHandler.handle(serverPacket); 

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
            serverSocket.close();
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}