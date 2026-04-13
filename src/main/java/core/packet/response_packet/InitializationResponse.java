/*
        Author: Nathaniel Brewer

*/

package core.packet.response_packet;

import core.identity.RuntimeMembershipState;
import core.packet.AbstractPacket;
import core.packet.PacketType;

public class InitializationResponse extends AbstractPacket {
    
    public InitializationResponse(RuntimeMembershipState membershipState, String recipientId) {
        super(membershipState.assignedId(), PacketType.INITIALIZATION_RES, membershipState.clusterId(), recipientId);
    }
}
