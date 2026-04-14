package core.packet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import core.connection.ConnectionManager;
import core.identity.RuntimeMembershipState;
import core.identity.TierRole;
import core.packet.initalization.InitalizationPacket;
import core.packet.initalization.InitalizationPacketManager;
import core.packet.message.MessagePacket;
import core.packet.message.MessagePacketManager;
import core.packet.response_packet.AckResponse;

class PacketManagerFactoryTest {

    private RuntimeMembershipState membershipState;

    @BeforeEach
    void setUp() {
        membershipState = new RuntimeMembershipState();
        membershipState.assignId("local-instance");
        membershipState.assignClusterId("cluster-a");
    }

    @Test
    void shouldResolveMessageManager() {
        MessagePacket packet = new MessagePacket(membershipState, "target-a");
        packet.addMessageBody("hello world");

        PacketProcessingContext context = PacketProcessingContext.builder()
            .receivedPacket(packet)
            .membershipState(membershipState)
            .instantiatorRole(TierRole.SERVER)
            .build();

        Optional<AbstractPacketManager> manager = PacketManagerFactory.tryCreateManager(context);

        assertTrue(manager.isPresent());
        assertEquals(MessagePacketManager.class, manager.get().getClass());
    }

    @Test
    void shouldReturnEmptyForResponsePacketTypes() {
        AckResponse packet = new AckResponse(membershipState, "target-a");

        PacketProcessingContext context = PacketProcessingContext.builder()
            .receivedPacket(packet)
            .membershipState(membershipState)
            .instantiatorRole(TierRole.SERVER)
            .build();

        Optional<AbstractPacketManager> manager = PacketManagerFactory.tryCreateManager(context);

        assertTrue(manager.isEmpty());
    }

    @Test
    void shouldResolveInitializationManagerWithConnectionContext() {
        InitalizationPacket packet = new InitalizationPacket(membershipState, "target-a");
        packet.addKeyValueToPayload("Server.listeningPort", "5004");

        PacketProcessingContext context = PacketProcessingContext.builder()
            .receivedPacket(packet)
            .membershipState(membershipState)
            .instantiatorRole(TierRole.COORDINATOR)
            .connectionManager(new StubConnectionManager(membershipState, TierRole.COORDINATOR))
            .senderIpAddress("127.0.0.1")
            .build();

        Optional<AbstractPacketManager> manager = PacketManagerFactory.tryCreateManager(context);

        assertTrue(manager.isPresent());
        assertEquals(InitalizationPacketManager.class, manager.get().getClass());
    }

    @Test
    void shouldRejectInitializationWhenConnectionContextMissing() {
        InitalizationPacket packet = new InitalizationPacket(membershipState, "target-a");
        packet.addKeyValueToPayload("Server.listeningPort", "5004");

        PacketProcessingContext context = PacketProcessingContext.builder()
            .receivedPacket(packet)
            .membershipState(membershipState)
            .instantiatorRole(TierRole.COORDINATOR)
            .build();

        assertThrows(IllegalArgumentException.class, () -> PacketManagerFactory.createManager(context));
    }

    private static final class StubConnectionManager extends ConnectionManager {

        private StubConnectionManager(RuntimeMembershipState membershipState, TierRole instantiatorRole) {
            super(membershipState, instantiatorRole);
        }

        @Override
        public boolean sendKeepAlive() {
            return true;
        }
    }
}
