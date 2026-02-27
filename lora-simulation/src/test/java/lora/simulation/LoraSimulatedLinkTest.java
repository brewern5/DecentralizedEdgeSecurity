package lora.simulation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import core.packet.AbstractPacket;
import core.sender.AbstractSender;
import lora.model.LoraConstraints;
import lora.protocol.LoraFrame;
import lora.protocol.LoraFrameCodec;

class LoraSimulatedLinkTest {

    @Test
    void transmitFrame_returnsFalse_whenFrameExceedsMtuEnvelope() {
        LoraConstraints constraints = new LoraConstraints(
                20,
                10,
                5000,
                5000,
                0,
                0,
                0.0d,
                0,
                0);

        LoraSimulatedLink link = new LoraSimulatedLink(new StubSender(), constraints);
        LoraFrameCodec codec = new LoraFrameCodec();
        byte[] chunk = "too-big".getBytes(StandardCharsets.UTF_8);
        LoraFrame frame = new LoraFrame("msg-1", 0, true, chunk, codec.computeChecksum(chunk));

        assertFalse(link.transmitFrame(frame));
    }

    @Test
    void transmitFrame_returnsTrue_whenFrameFitsAndNoLoss() {
        LoraConstraints constraints = new LoraConstraints(
                128,
                4,
                5000,
                5000,
                0,
                0,
                0.0d,
                0,
                0);

        LoraSimulatedLink link = new LoraSimulatedLink(new StubSender(), constraints);
        LoraFrameCodec codec = new LoraFrameCodec();
        byte[] chunk = "ok".getBytes(StandardCharsets.UTF_8);
        LoraFrame frame = new LoraFrame("msg-2", 0, true, chunk, codec.computeChecksum(chunk));

        assertTrue(link.transmitFrame(frame));
    }

    private static final class StubSender extends AbstractSender {

        @Override
        public boolean retry(AbstractPacket packet) {
            return false;
        }

        @Override
        public boolean send(AbstractPacket packet) {
            return true;
        }
    }
}
