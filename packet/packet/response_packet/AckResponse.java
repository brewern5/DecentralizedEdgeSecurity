/*
        Author: Nathaniel Brewer

*/

package packet.response_packet;

import packet.AbstractPacket;
import packet.PacketType;

public class AckResponse extends AbstractPacket{
    
    public AckResponse(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.ACK, clusterId, recipientId);
    }
    
}
