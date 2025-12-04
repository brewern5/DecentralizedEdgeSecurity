package packet.keep_alive;

import packet.AbstractPacket;
import packet.PacketType;

public class KeepAlivePacket extends AbstractPacket {
    
    public KeepAlivePacket(String senderId, String clusterId, String recipientId, boolean terminated) {
        super(senderId, PacketType.KEEP_ALIVE, clusterId, recipientId);

        // TODO: Maybe change this
        addKeyValueToPayload("Terminated", Boolean.toString(terminated));
    }

}
