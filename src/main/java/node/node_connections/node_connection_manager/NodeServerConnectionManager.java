/*
 *      Author: Nathaniel Brewer
 *      
 *      This is the connection manager that handles the server and the node's connection to it
 */
package node.node_connections.node_connection_manager;

import java.util.Iterator;
import java.util.Map;

import node.node_connections.NodeConnectionDto;
import node.node_connections.NodeConnectionDtoManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import node.node_packet.*;

public class NodeServerConnectionManager extends NodeConnectionManager {

        // Each class can have its own logger instance
    private static final Logger logger = LogManager.getLogger(NodeServerConnectionManager.class);
        
    // Step 1: (For my sanity) create an instance variable
    private static NodeServerConnectionManager instance;

    // Step 2: Get instance (if one is not there it is created then returned)
    public static synchronized NodeServerConnectionManager getInstance() {
        if (instance == null) {
            instance = new NodeServerConnectionManager();
        }
        return instance;
    }

    @Override
    public boolean sendKeepAlive(NodePacket keepAliveProbe) {

        boolean sent = false;
        logger.info(keepAliveProbe.toJson());
        Iterator<Map.Entry<String , NodeConnectionDto>> iterator =
            activeConnections.entrySet().iterator();

        while(iterator.hasNext()){
            Map.Entry<String, NodeConnectionDto> entry = iterator.next();
            sent = new NodeConnectionDtoManager(entry.getValue()).send(keepAliveProbe);
        }
        return sent;
    }

    public boolean sendPeerListReq(NodePacket peerListReq) {

        boolean sent = false;
        logger.info(peerListReq.toJson());
        Iterator<Map.Entry<String , NodeConnectionDto>> iterator =
            activeConnections.entrySet().iterator();


        while(iterator.hasNext()){
            Map.Entry<String, NodeConnectionDto> entry = iterator.next();
            sent = new NodeConnectionDtoManager(entry.getValue()).send(peerListReq);
        }

        return sent;
    }

}