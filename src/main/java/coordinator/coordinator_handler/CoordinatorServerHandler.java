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

package coordinator.coordinator_handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import coordinator.coordinator_connections.*;

/*
        NEW
*/

import coordinator.coordinator_connections.CoordinatorConnectionManager2;

import packet.AbstractPacket;
import packet.PacketType;
import packet.PacketManagerFactory;

import connection.ConnectionDto;
import connection.Priority;

import exception.;
/*
        END NEW
*/

import coordinator.coordinator_handler.coordinator_packet_type_handler.*;

import external.RuntimeTypeAdapterFactory;

import coordinator.edge_coordinator.EdgeCoordinator;
import exception.NonDelimitedPacket;


public class CoordinatorServerHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(CoordinatorServerHandler.class);

    //private static CoordinatorConnectionManager connectionManager = CoordinatorConnectionManager.getInstance();
    private static CoordinatorConnectionManager2 connectionManager = CoordinatorConnectionManager2.getInstance();

    private Socket serverSocket;
    private String serverIp;

    private AbstractPacket serverPacket;

    private BufferedReader reader;

    // Packet designed to be sent back to the initial sender, generic type so the type will need to be specified on instantiation
    private AbstractPacket responsePacket;


    public CoordinatorServerHandler(Socket socket) {
        this.serverSocket = socket;
    }


    /*        
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
                serverSocket.getOutputStream(), 
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
    public void run(){

        logger.info(
            "Server connected: \n\t"
            + serverSocket.getInetAddress().toString()
            + ":" 
            + serverSocket.getPort()
        );

        // Handle client events
        try{

            // This is what decodes the incoming packet
            reader = new BufferedReader(
                new InputStreamReader(
                    serverSocket.getInputStream()
                )
            );
            
            // Stores the payload as a string to check (and potentially remove) the delimiter
            String jsonPacket = reader.readLine();

            // Checks if the payload is properly terminated. If not, the packet is incomplete or an unsafe packet was sent
            if(jsonPacket.endsWith("||END||")){
                jsonPacket = jsonPacket.substring(0, jsonPacket.length() - "||END||".length());
            }
            else{
               throw new NonDelimitedPacket("Recieved Packet does not end with \" ||END|| \".");
            }

            // Reads the packet as json
            String json = jsonPacket;

            // Checks if empty packet
            if (json != null) {

                // Grabs the server IP in order to be saved in config file
                serverIp = serverSocket.getInetAddress().toString();
                serverIp = serverIp.substring(1); // Removes the forward slash

                // Pre-parses the Json in order to grab the packetType to use for the switch statement
                JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();

                String packetTypeStr = jsonObj.get("packetType").getAsString();

                

                // Covert the string to the ENUM packetType
                CoordinatorPacketType packetType = CoordinatorPacketType.valueOf(packetTypeStr);

                try {
                    // Reconstruct the recieved packet into a 'CoordinatorPacket' type 
                    serverPacket = buildGsonWithPacketSubtype(packetType, json);
                    //logger.info("Recieved:\n" + serverPacket.toJson());
                    readAction(packetType, serverPacket); 
                } catch(IllegalArgumentException e) {
                    logger.error("Recieved unknown packet of type: " + packetTypeStr);
                    return; // Early exit
                }
            }
        } catch (IOException e) {
            logger.error("IOException!" + e);
        } catch(NonDelimitedPacket e) {
            /* TODO: Create failure sender logic

                */
               logger.error("NonDelimitedPacket!" + e);
        }catch(Exception e) {
            logger.error("Unchecked Exception!" + e);
        }finally {
            try {
                if( reader != null){ reader.close(); }
                if( serverSocket != null && !serverSocket.isClosed()) { serverSocket.close(); }
            } catch (IOException e) {
                logger.error("Error closing socket!\n" + e);
            }
        }   
    }
}