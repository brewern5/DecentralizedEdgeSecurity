package lora.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import core.packet.AbstractPacket;
import core.packet.PacketType;
import core.sender.AbstractSender;
import lora.model.LoraConstraints;

class LoraTransportClientTest {

    @Test
    void send_delegatesAfterAllFragmentsTransmitSuccessfully() {
        LoraConstraints constraints = new LoraConstraints(
                96,
                8,
                5000,
                5000,
                0,
                0,
                0.0d,
                0,
                0);

        StubSender sender = new StubSender(true, "node-42");
        LoraTransportClient client = new LoraTransportClient(sender, constraints);

        TestPacket packet = new TestPacket(PacketType.INITIALIZATION);
        packet.addStringValue("payload", "phase2");

        boolean sent = client.send(packet);

        assertTrue(sent);
        assertEquals(1, sender.getSendCalls());
        assertEquals("node-42", client.getAssignedId());
    }

    @Test
    void send_doesNotDelegate_whenFragmentTransmissionFails() {
        LoraConstraints constraints = new LoraConstraints(
                96,
                8,
                5000,
                5000,
                0,
                0,
                1.0d,
                0,
                0);

        StubSender sender = new StubSender(true, "node-42");
        LoraTransportClient client = new LoraTransportClient(sender, constraints);

        TestPacket packet = new TestPacket(PacketType.MESSAGE);
        packet.addStringValue("payload", "drop-me");

        boolean sent = client.send(packet);

        assertFalse(sent);
        assertEquals(0, sender.getSendCalls());
    }

    private static final class StubSender extends AbstractSender {

        private final boolean sendResult;
        private int sendCalls;

        private StubSender(boolean sendResult, String assignedId) {
            this.sendResult = sendResult;
            this.assignedId = assignedId;
        }

        @Override
        public boolean retry(AbstractPacket packet) {
            return false;
        }

        @Override
        public boolean send(AbstractPacket packet) {
            sendCalls++;
            return sendResult;
        }

        private int getSendCalls() {
            return sendCalls;
        }
    }

    private static final class TestPacket extends AbstractPacket {

        private TestPacket(PacketType packetType) {
            super("sender", packetType, "cluster", "recipient");
        }
    }
}
