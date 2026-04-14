package core.packet.message;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import core.identity.RuntimeMembershipState;
import core.identity.TierRole;
import core.packet.AbstractPacket;
import core.packet.PacketType;

class MessagePacketManagerTest {

    private RuntimeMembershipState membershipState;

    @BeforeEach
    void setUp() {
        membershipState = new RuntimeMembershipState();
        membershipState.assignId("node-1");
        membershipState.assignClusterId("cluster-1");
    }

    @Test
    void shouldRespondWithAckForValidMessagePayload() {
        MessagePacket incomingPacket = new MessagePacket(membershipState, "server-1");
        incomingPacket.addMessageBody("hello");

        MessagePacketManager manager = new MessagePacketManager(
            membershipState,
            incomingPacket.getInstanceId(),
            TierRole.NODE
        );
        manager.recreateIncomingPacket(incomingPacket);

        AbstractPacket response = manager.processIncomingPacket();

        assertEquals(PacketType.ACK, response.getPacketType());
    }

    @Test
    void shouldRespondWithErrorWhenPayloadMissing() {
        MessagePacket incomingPacket = new MessagePacket(membershipState, "server-1");

        MessagePacketManager manager = new MessagePacketManager(
            membershipState,
            incomingPacket.getInstanceId(),
            TierRole.NODE
        );
        manager.recreateIncomingPacket(incomingPacket);

        AbstractPacket response = manager.processIncomingPacket();

        assertEquals(PacketType.ERROR, response.getPacketType());
    }
}
