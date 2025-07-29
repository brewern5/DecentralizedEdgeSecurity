/*
 *      Author: Nathaniel Brewer    
 * 
 *      This is for creating any sort of simple packet that does not have unique logic.
 * 
 *      Since most packets will be this way, this will be the default way to create most packets
 * 
 */
package coordinator.coordinator_packet.coordinator_packet_class;

import java.util.LinkedHashMap;

import coordinator.coordinator_packet.CoordinatorPacket;
import coordinator.coordinator_packet.CoordinatorPacketType;

public class CoordinatorGenericPacket extends CoordinatorPacket {

    // No payload
    public CoordinatorGenericPacket(CoordinatorPacketType packetType, String sender) {
        this.packetType = packetType;
        this.sender = sender;
        this.payload = new LinkedHashMap<String, String>();
    }
    // Constructor for setting a payload
    public CoordinatorGenericPacket(CoordinatorPacketType packetType, String sender, LinkedHashMap<String, String> payload) {
        this.packetType = packetType;
        this.sender = sender;
        this.payload = payload;

        payload.forEach( (key, value) -> {
            payloadPairCounter++;
        });
    }
    // Contructor with multiple value strings with no key
    public CoordinatorGenericPacket(CoordinatorPacketType packetType, String sender, String... value) {
        this.packetType = packetType;
        this.sender = sender;
        this.payload = new LinkedHashMap<String, String>();

        for(String val : value) {
            payload.put("message" + payloadPairCounter, val);
            payloadPairCounter++;
        }
    }

}
