/*
 *      Author: Nathaniel Brewer    
 * 
 *      This is for creating any sort of simple packet that does not have unique logic.
 * 
 *      Since most packets will be this way, this will be the default way to create most packets
 * 
 */
package coordinator_packet.coordinator_packet_class;

import java.util.LinkedHashMap;

import coordinator_packet.CoordinatorPacket;
import coordinator_packet.CoordinatorPacketType;

public class CoordinatorGenericPacket extends CoordinatorPacket {

    // No payload
    public CoordinatorGenericPacket(CoordinatorPacketType packetType, String sender) {
        this.packetType = packetType;
        this.sender = sender;
        this.payload = new LinkedHashMap<>();
    }
    // Constructor for setting a payload
    public CoordinatorGenericPacket(CoordinatorPacketType packetType, String sender, LinkedHashMap<String, String> payload) {
        this.packetType = packetType;
        this.sender = sender;
        this.payload = payload;
    }

}
