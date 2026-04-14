package core.packet.message;

import core.identity.RuntimeMembershipState;
import core.packet.AbstractPacket;
import core.packet.PacketType;

/**
 * Generic message packet used for data payload exchange.
 */
public class MessagePacket extends AbstractPacket {

    public MessagePacket(RuntimeMembershipState membershipState, String recipientId) {
        super(
            membershipState.assignedId(),
            PacketType.MESSAGE,
            membershipState.clusterId(),
            recipientId
        );
    }

    /**
     * Adds message content under a stable payload key.
     */
    public void addMessageBody(String body) {
        addKeyValueToPayload("message", body);
    }
}
