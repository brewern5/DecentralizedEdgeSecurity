/*
 *      Author: Nathaniel Brewer
 * 
 *      This will handle All peer requests to get a signature
 * 
 */
package node.node_handler.node_packet_type_handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NodeSigReq extends NodePacketHandler{
    
    private static final Logger logger = LogManager.getLogger(NodeSigReq.class);

    @Override
    public NodeHandlerResponse process() {

        try{

            // Get this Node's ID and append it to the end of the requesting Node's ID
            

            // Generates the success response to be put into the ack packet 
            packetResponse = new NodeHandlerResponse(true, "Recieved");

        } catch(Exception e) {
            logger.error("Error Handling packet" + e);
            // Generates the response to be put into the failure packet
            packetResponse = new NodeHandlerResponse(false, e, "Error Handling Packet.");
        }
        return packetResponse;
    }
}
