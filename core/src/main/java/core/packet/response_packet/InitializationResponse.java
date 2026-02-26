/*
        Author: Nathaniel Brewer

*/

package core.packet.response_packet;

import core.packet.AbstractPacket;
import core.packet.PacketType;

public class InitializationResponse extends AbstractPacket {
    
    public InitializationResponse(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.INITIALIZATION_RES, clusterId, recipientId);
    }
}
