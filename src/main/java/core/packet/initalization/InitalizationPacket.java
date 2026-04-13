/*
 *      Author: Nathaniel Brewer
 * 
 */
package core.packet.initalization;

import core.identity.RuntimeMembershipState;
import core.packet.AbstractPacket;
import core.packet.PacketType;

public class InitalizationPacket extends AbstractPacket{

    public InitalizationPacket(RuntimeMembershipState membershipState, String recipientId) {
        super(membershipState.assignedId(), PacketType.INITIALIZATION, membershipState.clusterId(), recipientId);
    }
}
