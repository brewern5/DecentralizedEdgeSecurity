/*
        Author: Nathaniel Brewer

*/

package core.packet.response_packet;

import core.packet.AbstractPacket;
import core.packet.PacketType;

public class ErrorResponse extends AbstractPacket{

    public ErrorResponse(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.ERROR, clusterId, recipientId);
    }
}
