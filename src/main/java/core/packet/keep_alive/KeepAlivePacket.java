package core.packet.keep_alive;

import core.packet.AbstractPacket;
import core.packet.PacketType;

import core.identity.RuntimeMembershipState;

public class KeepAlivePacket extends AbstractPacket {
    
    public KeepAlivePacket(RuntimeMembershipState membershipState, String recipientId, boolean terminated) {
        super(membershipState.assignedId(), PacketType.KEEP_ALIVE, membershipState.clusterId(), recipientId);

        /*  TODO: termination needs to only be apart of parent clusters. For example, a node sending a
            keep_alive to the server will not need to have the "terminated" 
         */
    }

    public void addTerminationStatus(boolean terminate) {
        addKeyValueToPayload("Terminated", Boolean.toString(terminate));
    }

}
