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

package components.coordinator.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;

import components.coordinator.connections.CoordinatorConnectionManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.exception.NonDelimitedPacket;
import core.external.PacketTypeAdapterFactory;
import core.identity.AbstractTierIdentity;
import core.packet.AbstractPacket;
import core.packet.AbstractPacketManager;
import core.packet.PacketManagerFactory;
import core.packet.PacketType;
 
public class CoordinatorServerHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(CoordinatorServerHandler.class);

    private Socket serverSocket;
    private String serverIp;

    private AbstractPacketManager serverPacketManager;
    private AbstractPacket serverPacket;

    private BufferedReader reader;

    private AbstractPacket responsePacket;

    private final AbstractTierIdentity identity;

    public CoordinatorServerHandler(Socket socket, AbstractTierIdentity identity) {
        this.serverSocket = socket;
        this.identity = identity;
    }


    /*        
     *          Respond
     */


    private void respond() {

        String json = responsePacket.toDelimitedString();
        
        try{
            PrintWriter output = new PrintWriter(
                serverSocket.getOutputStream(), 
                true
            );
            output.println(json);
            output.close();
        } catch (IOException e) {
            logger.error("Error sending response packet of type: {}\n", responsePacket.getPacketType(), e);
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

        try{

            reader = new BufferedReader(
                new InputStreamReader(
                    serverSocket.getInputStream()
                )
            );
            String jsonPacket = reader.readLine();

            if(jsonPacket.endsWith("||END||")){
                jsonPacket = jsonPacket.substring(
                    0, 
                    jsonPacket.length() - "||END||".length()
                );
            }
            else{
               throw new NonDelimitedPacket("Recieved Packet does not end with \" ||END|| \".");
            }

            String json = jsonPacket;

            if (json != null) {

                serverIp = serverSocket.getInetAddress().toString();
                serverIp = serverIp.substring(1);

                try {
                       Gson gson = new GsonBuilder()
                        .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
                        .create();

                    serverPacket = gson.fromJson(json, AbstractPacket.class);
                        
                    if (!PacketManagerFactory.requiresManager(serverPacket.getPacketType())) {
                        logger.warn("Received response packet type: " + serverPacket.getPacketType());
                        return;
                    }   

                    CoordinatorConnectionManager connectionManager = CoordinatorConnectionManager.getInstance();
                    
                    String coordinatorId = connectionManager.getInstanceId();
                    logger.info("Coordinator Instance ID: " + coordinatorId);

                    if (serverPacket.getPacketType() == PacketType.INITIALIZATION) {
                        serverPacketManager = PacketManagerFactory.createManager(
                            serverPacket,
                            coordinatorId,
                            connectionManager.getClusterId(),
                            identity.getRole(),
                            connectionManager,
                            serverSocket.getInetAddress().getHostAddress()
                        );
                    } else {
                        serverPacketManager = PacketManagerFactory.createManager(
                            serverPacket,
                            coordinatorId,
                            connectionManager.getClusterId(),
                            identity.getRole()
                        ); 
                    } 

                    responsePacket = serverPacketManager.processIncomingPacket();
                    respond();

                } catch(IllegalArgumentException e) { 
                    logger.error("Recieved unknown packet!  " + e);  
                    return;
                }
            } 
        } catch (IOException e) {
            logger.error("IOException!" + e);
        } catch(NonDelimitedPacket e) {
            /* TODO: Create failure sender logic

                */
               logger.error("NonDelimitedPacket!" + e);
        } catch(Exception e) {
            logger.error("Unchecked Exception!" + e);
        } finally {
            try {
                if( reader != null){ reader.close(); }
                if( serverSocket != null && !serverSocket.isClosed()) { serverSocket.close(); }
            } catch (IOException e) {
                logger.error("Error closing socket!\n" + e);
            }
        }   
    }
}