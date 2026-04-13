/*
    Author: Nathaniel Brewer

*/

package core.packet.response_packet;

import core.identity.RuntimeMembershipState;
import core.packet.AbstractPacket;
import core.packet.PacketType;

public class ErrorResponse extends AbstractPacket{

    public ErrorResponse(RuntimeMembershipState membershipState, String recipientId) {
        super(membershipState.assignedId(), PacketType.ERROR, membershipState.clusterId(), recipientId);
    }
}
