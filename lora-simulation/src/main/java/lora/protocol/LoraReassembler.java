/*
    Author: Nathaniel Brewer

    Reconstructor for the fragmentation of messages for the LoRa simulation. 
    Since LoRa is limited in it's bandwidth, large packets ~ (>= 250 bytes) need to
    be fragmented in order to be sent. 
*/
package lora.protocol;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Reassembles LoRa frames for a single message stream.
 */
public final class LoraReassembler {

    private final LoraFrameCodec codec;

    private String activeMessageId;
    private Integer lastFragmentIndex;
    private final Map<Integer, byte[]> fragments = new TreeMap<>();

    public LoraReassembler() {
        this(new LoraFrameCodec());
    }

    public LoraReassembler(LoraFrameCodec codec) {
        this.codec = codec;
    }

    public Optional<byte[]> accept(LoraFrame frame) {
        if (!frame.verifyChecksum(codec)) {
            throw new IllegalArgumentException("Invalid frame checksum");
        }

        if (activeMessageId == null) {
            activeMessageId = frame.getMessageId();
        } else if (!activeMessageId.equals(frame.getMessageId())) {
            throw new IllegalArgumentException("Unexpected messageId. Expected " + activeMessageId + " but got " + frame.getMessageId());
        }

        byte[] existing = fragments.get(frame.getFragmentIndex());
        if (existing != null) {
            if (!Arrays.equals(existing, frame.getPayloadChunk())) {
                throw new IllegalStateException("Conflicting duplicate fragment at index " + frame.getFragmentIndex());
            }
        } else {
            fragments.put(frame.getFragmentIndex(), frame.getPayloadChunk());
        }

        if (frame.isLastFragment()) {
            if (lastFragmentIndex != null && !lastFragmentIndex.equals(frame.getFragmentIndex())) {
                throw new IllegalStateException("Multiple conflicting last fragment indexes");
            }
            lastFragmentIndex = frame.getFragmentIndex();
        }

        if (!isComplete()) {
            return Optional.empty();
        }

        byte[] assembled = assemble();
        reset();
        return Optional.of(assembled);
    }

    public void reset() {
        activeMessageId = null;
        lastFragmentIndex = null;
        fragments.clear();
    }

    private boolean isComplete() {
        if (lastFragmentIndex == null) {
            return false;
        }
        if (fragments.size() != lastFragmentIndex + 1) {
            return false;
        }

        for (int i = 0; i <= lastFragmentIndex; i++) {
            if (!fragments.containsKey(i)) {
                return false;
            }
        }
        return true;
    }

    private byte[] assemble() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (int i = 0; i <= lastFragmentIndex; i++) {
            byte[] chunk = fragments.get(i);
            output.write(chunk, 0, chunk.length);
        }
        return output.toByteArray();
    }
}
