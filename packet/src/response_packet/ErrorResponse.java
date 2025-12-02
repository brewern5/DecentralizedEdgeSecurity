/*
        Author: Nathaniel Brewer

*/

package src.response_packet;

import src.AbstractPacket;
import src.PacketType;

public class ErrorResponse extends AbstractPacket{

    protected PacketType packetType = PacketType.ERROR;

    public ErrorResponse(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.ERROR, clusterId, recipientId);
    }
}
