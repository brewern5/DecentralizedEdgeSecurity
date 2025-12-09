/*
 *      Author: Nathaniel Brewer
 * 
 *      This class will handle all things to do with the NodePeerListRequest
 * 
 */
package node.node_services;

import java.util.LinkedHashMap;

import node.node_packet.*;
import node.node_packet.node_packet_class.*;
import java.util.UUID;

public class NodePeerListService {
    
    public static NodePacket createPeerListReq(String nodeId, String nodeIp, String clusterId) {

        LinkedHashMap<String, String> payload = new LinkedHashMap<>();
        
        payload.put("req_id", UUID.randomUUID().toString());
        payload.put("cluster_id", clusterId);
        payload.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return new NodeGenericPacket(
            NodePacketType.PEER_LIST_REQ,
            nodeId,
            payload
        );
    }
}
