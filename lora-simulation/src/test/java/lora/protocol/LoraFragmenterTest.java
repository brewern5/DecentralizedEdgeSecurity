package lora.protocol;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import lora.model.LoraConstraints;

class LoraFragmenterTest {

    @Test
    void fragment_splitsPayload_andEachFrameFitsMtuEnvelope() {
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

        LoraFrameCodec codec = new LoraFrameCodec();
        LoraFragmenter fragmenter = new LoraFragmenter(constraints, codec);

        byte[] payload = "The quick brown fox jumps over the lazy dog. This payload should span multiple LoRa frames."
                .getBytes(StandardCharsets.UTF_8);

        List<LoraFrame> frames = fragmenter.fragment("msg-boundary", payload);

        assertTrue(frames.size() > 1, "Expected fragmentation into multiple frames");
        for (LoraFrame frame : frames) {
            String encoded = codec.encode(frame);
            int envelope = constraints.computeEnvelopeSizeBytes(codec.utf8Length(encoded));
            assertTrue(envelope <= constraints.getMtuBytes(), "Frame exceeds MTU envelope");
        }

        assertFalse(frames.get(0).isLastFragment());
        assertTrue(frames.get(frames.size() - 1).isLastFragment());
    }

    @Test
    void fragment_throwsWhenMtuCannotFitFrameMetadata() {
        LoraConstraints constraints = new LoraConstraints(
                12,
                10,
                5000,
                5000,
                0,
                0,
                0.0d,
                0,
                0);

        LoraFragmenter fragmenter = new LoraFragmenter(constraints);

        assertThrows(IllegalArgumentException.class,
                () -> fragmenter.fragment("msg-too-small", "x".getBytes(StandardCharsets.UTF_8)));
    }
}
