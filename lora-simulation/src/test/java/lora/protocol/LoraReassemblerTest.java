package lora.protocol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import lora.model.LoraConstraints;

class LoraReassemblerTest {

    @Test
    void accept_reassemblesOutOfOrderFrames_whenAllFragmentsArrive() {
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

        byte[] original = "Out-of-order reassembly test payload for LoRa fragment protocol"
                .getBytes(StandardCharsets.UTF_8);

        LoraFragmenter fragmenter = new LoraFragmenter(constraints);
        List<LoraFrame> frames = new ArrayList<>(fragmenter.fragment("msg-reassemble", original));
        Collections.reverse(frames);

        LoraReassembler reassembler = new LoraReassembler();

        Optional<byte[]> assembled = Optional.empty();
        for (LoraFrame frame : frames) {
            assembled = reassembler.accept(frame);
        }

        assertTrue(assembled.isPresent(), "Expected completed reassembly after final fragment");
        assertArrayEquals(original, assembled.get());
    }

    @Test
    void accept_throwsOnConflictingDuplicateFragment() {
        LoraFrameCodec codec = new LoraFrameCodec();

        byte[] first = "abc".getBytes(StandardCharsets.UTF_8);
        byte[] conflicting = "xyz".getBytes(StandardCharsets.UTF_8);

        LoraFrame frameA = new LoraFrame("dup-msg", 0, false, first, codec.computeChecksum(first));
        LoraFrame frameB = new LoraFrame("dup-msg", 0, false, conflicting, codec.computeChecksum(conflicting));

        LoraReassembler reassembler = new LoraReassembler(codec);
        reassembler.accept(frameA);

        assertThrows(IllegalStateException.class, () -> reassembler.accept(frameB));
    }
}
