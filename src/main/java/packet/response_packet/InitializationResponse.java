/*
        Author: Nathaniel Brewer

*/

package packet.response_packet;

import packet.AbstractPacket;
import packet.PacketType;

public class InitializationResponse extends AbstractPacket {
    
    public InitializationResponse(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.INITIALIZATION_RES, clusterId, recipientId);
    }
}
