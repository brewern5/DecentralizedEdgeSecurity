/*      Author: Nathaniel Brewer
 * 
 *      This is the handler for the Packet Type PEER_LIST_REQ. This extends the Abstract
 *      class 'Packet handler'
 *      
 *      This will be the second packet recieved from a node. The node will request this list
 *      from the server. The server will return a list of nodes with the data:
 *          (nodeId, nodeIp, nodePort)
 *      Alongside the list of nodes this server will send: ClusterId, Timestamp, RequestId, ResponseId
 * 
 */
package server.server_handler.server_packet_type_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_connections.server_connection_manager.ServerConnectionManager;
import server.server_connections.server_connection_manager.ServerNodeConnectionManager;

public class ServerPeerReqHandler extends ServerPacketHandler{

    private static final Logger logger = LogManager.getLogger(ServerPeerReqHandler.class);

    public 
   
    public ServerPeerReqHandler() {
        connectionManager = ServerNodeConnectionManager.getInstance();
    }

    /* This method will be called from the 'PacketHandler' SuperClass's "handle" method.
     * This particular method will seperate the handled KeyValue pairs and seperate them
     * into a key and a value. In this instance it is the Servers preferred recieving port.
     * Once recieved and handled, this method will put the Key Value into the config file
     */
    @Override
    public ServerHandlerResponse process() {

        try{
            payloadKeyValuePairs.forEach( (k, v) -> { 
                //System.out.println("Message " + messageCounter + ":\n\t\t" + v );


            });

            // Generates the success response to be put into the ack packet 
            packetResponse = new ServerHandlerResponse(true, "Recieved");

        } catch(Exception e) {
            logger.error("Error Handling packet\n"+e);
            // Generates the response to be put into the failure packet
            packetResponse = new ServerHandlerResponse(false, e, "Error Handling Packet.");
        }
        return packetResponse;
    }
}

}
