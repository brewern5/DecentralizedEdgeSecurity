/*
        Author: Nathaniel Brewer

*/

package src.response_packet;

import src.AbstractPacket;
import src.PacketType;

public class AckResponse extends AbstractPacket{
    
    public AckResponse(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.ACK, clusterId, recipientId);
    }
    
}
