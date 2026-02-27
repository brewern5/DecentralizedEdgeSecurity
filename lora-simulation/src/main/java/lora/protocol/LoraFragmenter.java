/*
    Author: Nathaniel Brewer

    Due to the limitations of LoRa devices, we need logic to divide the packets, into
    LoRa-Constraint safe, packets. AKA Fragmentation.  

    This will fragment the total packet into frames. Each frame is 1 LoRa packet
*/
package lora.protocol;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import lora.model.LoraConstraints;

public final class LoraFragmenter {

    private final LoraConstraints constraints;
    private final LoraFrameCodec codec;

    public LoraFragmenter(LoraConstraints constraints) {
        this(constraints, new LoraFrameCodec());
    }

    public LoraFragmenter(LoraConstraints constraints, LoraFrameCodec codec) {
        this.constraints = constraints;
        this.codec = codec;
    }

    public List<LoraFrame> fragment(byte[] payload) {
        return fragment(UUID.randomUUID().toString(), payload);
    }

    public List<LoraFrame> fragment(String messageId, byte[] payload) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId must not be blank");
        }
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }

        if (payload.length == 0) {
            LoraFrame frame = buildFrame(messageId, 0, true, new byte[0]);
            if (!fitsMtu(frame)) {
                throw mtuTooSmallException();
            }
            return List.of(frame);
        }

        List<LoraFrame> frames = new ArrayList<>();
        int offset = 0;
        int fragmentIndex = 0;

        while (offset < payload.length) {
            int remaining = payload.length - offset;
            int chunkSize = remaining;
            LoraFrame selected = null;

            while (chunkSize > 0) {
                boolean isLast = (offset + chunkSize) >= payload.length;
                byte[] chunk = Arrays.copyOfRange(payload, offset, offset + chunkSize);
                LoraFrame candidate = buildFrame(messageId, fragmentIndex, isLast, chunk);

                if (fitsMtu(candidate)) {
                    selected = candidate;
                    break;
                }
                chunkSize--;
            }

            if (selected == null) {
                throw mtuTooSmallException();
            }

            frames.add(selected);
            offset += selected.getPayloadLength();
            fragmentIndex++;
        }

        return Collections.unmodifiableList(frames);
    }

    public List<LoraFrame> fragment(String payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }
        return fragment(payload.getBytes(StandardCharsets.UTF_8));
    }

    boolean fitsMtu(LoraFrame frame) {
        String encoded = codec.encode(frame);
        int encodedBytes = codec.utf8Length(encoded);
        int envelopeSize = constraints.computeEnvelopeSizeBytes(encodedBytes);
        return envelopeSize <= constraints.getMtuBytes();
    }

    private LoraFrame buildFrame(String messageId, int fragmentIndex, boolean isLast, byte[] chunk) {
        long checksum = codec.computeChecksum(chunk);
        return new LoraFrame(messageId, fragmentIndex, isLast, chunk, checksum);
    }

    private IllegalArgumentException mtuTooSmallException() {
        return new IllegalArgumentException("LoRa MTU is too small to carry even the minimum frame metadata");
    }
}
