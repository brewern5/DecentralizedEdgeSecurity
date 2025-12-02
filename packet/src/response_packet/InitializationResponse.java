/*
        Author: Nathaniel Brewer

*/

package src.response_packet;

import src.AbstractPacket;
import src.PacketType;

public class InitializationResponse extends AbstractPacket {
    
    public InitializationResponse(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.INITIALIZATION_RES, clusterId, recipientId);

        // TODO: Populate the initalization 
    }
}
