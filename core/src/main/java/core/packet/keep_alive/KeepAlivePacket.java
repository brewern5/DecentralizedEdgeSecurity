package core.packet.keep_alive;

import core.packet.AbstractPacket;
import core.packet.PacketType;

public class KeepAlivePacket extends AbstractPacket {
    
    public KeepAlivePacket(String senderId, String clusterId, String recipientId, boolean terminated) {
        super(senderId, PacketType.KEEP_ALIVE, clusterId, recipientId);

        /*  TODO: termination needs to only be apart of parent clusters. For example, a node sending a
            keep_alive to the server will not need to have the "terminated" 
         */
    }

    public void addTerminationStatus(boolean terminate) {
        addKeyValueToPayload("Terminated", Boolean.toString(terminate));
    }

}
