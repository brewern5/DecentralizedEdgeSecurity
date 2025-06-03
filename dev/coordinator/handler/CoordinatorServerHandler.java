package handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;

import config.CoordinatorConfig; // The configuration file for the entire network
import packet.*;

public class CoordinatorServerHandler implements Runnable {

    private volatile boolean running = true;

    private CoordinatorConfig config = new CoordinatorConfig();

    private Socket serverSocket;
    private String serverIP;

    private CoordinatorPacket serverPacket;

    // Packet designed to be sent back to the initial sender, generic type so the type will need to be specified on instantiation
    private CoordinatorPacket responsePacket;

    public CoordinatorServerHandler(Socket socket) {
        this.serverSocket = socket;
    }

    /*
     * 
     *                      Packet handling by type
     * 
     */

    // Sets up a new server and will store the necessary data in the config file (eventually some sort of auth)
    private void init() {
        // TODO: handle initalization from here
        serverIP = serverSocket.getInetAddress().toString();
        serverIP = serverIP.substring(1); // Removes the forward slash
        
        // Grab the payload from the packet
        String payload = serverPacket.getPayload();

        String[] pairs = payload.split(";");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0];
            String value = keyValue.length > 1 ? keyValue[1] : ""; // makes sure the value is always a valid string

            // Write the key value to coordinatorConfig
            config.writeToConfig(key, value);
        }
        
        // TODO: Instatiate returnPacket
        responsePacket = new CoordinatorPacket(
                CoordinatorPacketType.ACK,   // Packet type
                "Coordinator",   // Sender
                "Good"          // Payload
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

        // Stores the IP address from the sender
        serverIP = serverSocket.getInetAddress().toString();
        serverIP = serverIP.substring(1); // Removes the forward slash

        // Handle client events
        try{
            System.out.println("\n\n\t\t Recieved New Packet!\n\n");
            BufferedReader reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            // Reads the packet as json
            String json = reader.readLine();

            // Checks if empty packet
            if (json != null) {
                
                // Instantiates a new packet with the json 
                serverPacket = gson.fromJson(json, CoordinatorPacket.class);

                // Checks the packet type to determine how it needs to handle it
                switch (serverPacket.getPacketType()) {
                    case INITIALIZATION:
                        init();
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
                        // Handle unknown or unsupported packet types
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
