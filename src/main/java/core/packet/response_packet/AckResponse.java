/*
        Author: Nathaniel Brewer

*/

package core.packet.response_packet;

import core.identity.RuntimeMembershipState;
import core.packet.AbstractPacket;
import core.packet.PacketType;

public class AckResponse extends AbstractPacket{
    
    public AckResponse(RuntimeMembershipState membershipState, String recipientId) {
        super(membershipState.assignedId(), PacketType.ACK, membershipState.clusterId(), recipientId);
    }
    
}
