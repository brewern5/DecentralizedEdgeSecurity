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

package coordinator_handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.RuntimeTypeAdapter;

import coordinator_handler.coordinator_packet_handler.*;
import coordinator_packet.CoordinatorPacket;
import coordinator_packet.coordinator_packet_class.*;
import coordinator_packet.CoordinatorPacketType;

public class CoordinatorServerHandler implements Runnable {

    private Socket serverSocket;
    private String serverIP;

    private CoordinatorGenericPacket serverPacket;

    // Packet designed to be sent back to the initial sender, generic type so the type will need to be specified on instantiation
    private CoordinatorPacket responsePacket;

    public CoordinatorServerHandler(Socket socket) {
        this.serverSocket = socket;
    }

    // This is a good response, it will be sent back to the server to ensure a packet was recieved 
    private void ack(CoordinatorHandlerResponse packetResponse) {

        responsePacket = new CoordinatorGenericPacket(
                CoordinatorPacketType.ACK,
                "Coordinator",
                packetResponse.combineMaps()
        );
        respond();
    }

    // This is the creation of the failure packet based on the packet response 
    private void failure(CoordinatorHandlerResponse packetResponse) {
        
        // Construct a new failure packet
        responsePacket = new CoordinatorGenericPacket(
                CoordinatorPacketType.ERROR,    // Packet type
                "Coordinator",  // Sender
                packetResponse.combineMaps()  // Payload
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
    public void run(){

        Gson gson = new Gson();     // Allows us to decode the json string from the message

        System.out.println(
            "Server connected from "
            + serverSocket.getInetAddress().toString()
            + ":" 
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
                payload = payload.substring(0, payload.length() - "||END||".length());
            }
            else{
                failure(new CoordinatorHandlerResponse(
                    false, 
                    new Exception("Payload not terminated."), 
                    "Incomplete Packet")
                );
            }

            // Reads the packet as json
            String json = payload;

            // Checks if empty packet
            if (json != null) {
                CoordinatorPacketHandler packetHandler;    // This will will be instantiated based on the PacketType that needs to handle this. I.e initalizationHandler

                CoordinatorHandlerResponse packetResponse; // The response from the handling of the packet.    
                
                // Grabs the server IP in order to be saved in config file
                serverIP = serverSocket.getInetAddress().toString();
                serverIP = serverIP.substring(1); // Removes the forward slash

                // This is needed in order to construct the packet class from Gson, it will throw an error
                // without this package
                RuntimeTypeAdapterFactory<CoordinatorPacket> packetAdapterFactory=
                    
                
                // Instantiates a new packet with the json 
                serverPacket = gson.fromJson(json, CoordinatorGenericPacket.class);

                // Checks the packet type to determine how it needs to handle it
                switch (serverPacket.getPacketType()) {
                    case INITIALIZATION:

                        System.out.println("\nRecieved:\n\n" + serverPacket.toString() + "\n\n");

                        // Create the packetType based handler
                        packetHandler = new CoordinatorInitalizationHandler();

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