/*
        Author: Nathaniel Brewer

*/

package core.packet.response_packet;

import core.packet.AbstractPacket;
import core.packet.PacketType;

public class AckResponse extends AbstractPacket{
    
    public AckResponse(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.ACK, clusterId, recipientId);
    }
    
}
