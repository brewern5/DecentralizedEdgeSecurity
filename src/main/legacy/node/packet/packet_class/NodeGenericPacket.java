/*
 *      Author: Nathaniel Brewer    
 * 
 *      This is for creating any sort of simple packet that does not have unique logic.
 * 
 *      Since most packets will be this way, this will be the default way to create most packets
 * 
 */
package components.node.packet.packet_class;

import java.util.LinkedHashMap;

import components.node.packet.*;

public class NodeGenericPacket extends NodePacket {

    public NodeGenericPacket(NodePacketType packetType, String id) {
        this.packetType = packetType;
        this.id = id;
        this.payload = new LinkedHashMap<>();
    }
    
    public NodeGenericPacket(NodePacketType packetType, String id, LinkedHashMap<String, String> payload) {
        this.packetType = packetType;
        this.id = id;
        this.payload = payload;

        payload.forEach( (key, value) -> {
            payloadPairCounter++;
        });
    }
    
    public NodeGenericPacket(NodePacketType packetType, String id, String... value) {
        this.packetType = packetType;
        this.id = id;
        this.payload = new LinkedHashMap<String, String>();

        for(String val : value) {
            payload.put("message" + payloadPairCounter, val);
            payloadPairCounter++;
        }
    }
}
