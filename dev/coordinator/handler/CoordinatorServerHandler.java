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

        // TODO: Instatiate returnPacket

    }

    /*
     *                     Acknowledgment
     */

     // Sends the response packet back to sender
    private void sendAck() {

    }

    /*
     * 
     *                      Main run loop
     * 
     */


    @Override
    public void run(){

        Gson gson = new Gson();     // Allows us to decode the json string from the message

        System.out.println("Server connected from "+ serverSocket.getInetAddress().toString()+ ":" + serverSocket.getLocalPort());

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
                        // TODO: Handle initialization logic
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

                PrintWriter output = new PrintWriter(serverSocket.getOutputStream(), true);
                output.println("Hi, edge server, this is the edge coordinator!");
            }

            // TODO: Do stuff here with message

            // Acknowledge

            reader.close();

            // TODO: check some other things before closing the sockets
            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }      
    }
}
