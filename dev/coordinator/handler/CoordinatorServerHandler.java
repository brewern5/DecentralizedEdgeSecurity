package handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.util.List;
import java.util.ArrayList;

import java.net.Socket;

import com.google.gson.Gson;

import config.CoordinatorConfig; // The configuration file for the entire network
import packet.*;

import handler.packet_handler.*;

public class CoordinatorServerHandler implements Runnable {

    private Socket serverSocket;
    private String serverIP;

    private CoordinatorPacket serverPacket;

    // Packet designed to be sent back to the initial sender, generic type so the type will need to be specified on instantiation
    private CoordinatorPacket responsePacket;

    public CoordinatorServerHandler(Socket socket) {
        this.serverSocket = socket;
    }

    // This is a good response, it will be sent back to the server to ensure a packet was recieved 
    private void ack(HandlerResponse packetResponse) {

        responsePacket = new CoordinatorPacket(
                CoordinatorPacketType.ACK,  // Packet type
                "Coordinator",       // Sender
                packetResponse.toString()   // Payload
        );
        respond();
    }

    private void failure(HandlerResponse packetResponse) {
        
        responsePacket = new CoordinatorPacket(
                CoordinatorPacketType.ERROR,  // Packet type
                "Coordinator",       // Sender
                packetResponse.toString()   // Payload
        );
        respond();
    }

    /*
     *                     Acknowledgment
     */

    // Takes an already initalized response packet and returns to sender
    private void respond() {

        String json = responsePacket.toString();

        try{
            PrintWriter output = new PrintWriter(serverSocket.getOutputStream(), true);
            output.println(json);
        } catch (IOException e) {
            System.err.println("Error sending response packet of type: " + responsePacket.getPacketType());
            e.printStackTrace();
        }
    }

    /*
     * 
     *                      Main run loop
     * 
     */


    @Override
    public void run(){

        Gson gson = new Gson();     // Allows us to decode the json string from the message

        System.out.println("Server connected from "+ serverSocket.getInetAddress().toString()+ ":" + serverSocket.getPort());

        // Handle client events
        try{
            System.out.println("\n\n\t\t Recieved New Packet!\n\n");
            BufferedReader reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            String payload = reader.readLine();

            // Checks if the payload is properly terminated. If not, the packet is incomplete or an unsafe packet was sent
            if(payload.endsWith("||END||")){
                payload = payload.substring(0, payload.length() - "||END||".length());
            }
            else{
                failure(new HandlerResponse(
                    false, 
                    new Exception("Payload not terminated."), 
                    "Incomplete Packet")
                );
            }

            // Reads the packet as json
            String json = payload;

            // Checks if empty packet
            if (json != null) {

                PacketHandler packetHandler;    // This will will be instantiated based on the PacketType that needs to handle this. I.e initalizationHandler

                HandlerResponse packetResponse; // The response from the handling of the packet.    
                
                // Grabs the server IP in order to be saved in config file
                serverIP = serverSocket.getInetAddress().toString();
                serverIP = serverIP.substring(1); // Removes the forward slash
                
                // Instantiates a new packet with the json 
                serverPacket = gson.fromJson(json, CoordinatorPacket.class);

                // Checks the packet type to determine how it needs to handle it
                switch (serverPacket.getPacketType()) {
                    case INITIALIZATION:

                        System.out.println("\nRecieved:\n\n" + serverPacket.toString() + "\n\n");

                        packetHandler = new InitalizationHandler();
                        packetResponse = packetHandler.handle(serverPacket); 

                        // If good send ack
                        if(packetResponse.getSuccess() == true){
                            ack(packetResponse);
                        }
                        else if(packetResponse.getSuccess() == false){
                            System.err.println("Error Handling Packet of Type: \tINITIALIZATION\n\nDetails:");
                            packetResponse.printMessages();
                          
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
