/*
        Author: Nathaniel Brewer

*/

package packet.response_packet;

import packet.AbstractPacket;
import packet.PacketType;

public class ErrorResponse extends AbstractPacket{

    protected PacketType packetType = PacketType.ERROR;

    public ErrorResponse(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.ERROR, clusterId, recipientId);
    }
}
