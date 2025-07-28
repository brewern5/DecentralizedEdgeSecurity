/*
 *      Author: Nathaniel Brewer    
 * 
 *      This is for creating any sort of simple packet that does not have unique logic.
 * 
 *      Since most packets will be this way, this will be the default way to create most packets
 * 
 */
package server_packet.server_packet_class;

import java.util.LinkedHashMap;

import server_packet.*;

public class ServerGenericPacket extends ServerPacket {

    // No payload
    public ServerGenericPacket(ServerPacketType packetType, String sender) {
        this.packetType = packetType;
        this.sender = sender;
        this.payload = new LinkedHashMap<>();
    }
    
    // Constructor for setting a payload
    public ServerGenericPacket(ServerPacketType packetType, String sender, LinkedHashMap<String, String> payload) {
        this.packetType = packetType;
        this.sender = sender;
        this.payload = payload;

        payload.forEach( (key, value) -> {
            payloadPairCounter++;
        });
    }

    // Contructor with multiple value strings with no key
    public ServerGenericPacket(ServerPacketType packetType, String sender, String... value) {
        this.packetType = packetType;
        this.sender = sender;
        this.payload = new LinkedHashMap<>();

        for(String val : value) {
            payload.put("message" + payloadPairCounter, val);
            payloadPairCounter++;
        }
    }
}
