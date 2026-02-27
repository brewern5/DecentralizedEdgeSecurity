/*
    Author: Nathaniel Brewer

    Immutable LoRa transport frame carrying one fragment of a larger message.
 
*/
package lora.protocol;

import java.util.Arrays;
import java.util.Objects;

public final class LoraFrame {

    private final String messageId;
    private final int fragmentIndex;
    private final boolean lastFragment;
    private final byte[] payloadChunk;
    private final long checksum;

    public LoraFrame(String messageId, int fragmentIndex, boolean lastFragment, byte[] payloadChunk, long checksum) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId must not be blank");
        }
        if (fragmentIndex < 0) {
            throw new IllegalArgumentException("fragmentIndex must be >= 0");
        }
        if (payloadChunk == null) {
            throw new IllegalArgumentException("payloadChunk must not be null");
        }

        this.messageId = messageId;
        this.fragmentIndex = fragmentIndex;
        this.lastFragment = lastFragment;
        this.payloadChunk = Arrays.copyOf(payloadChunk, payloadChunk.length);
        this.checksum = checksum;
    }

    public String getMessageId() {
        return messageId;
    }

    public int getFragmentIndex() {
        return fragmentIndex;
    }

    public boolean isLastFragment() {
        return lastFragment;
    }

    public byte[] getPayloadChunk() {
        return Arrays.copyOf(payloadChunk, payloadChunk.length);
    }

    public int getPayloadLength() {
        return payloadChunk.length;
    }

    public long getChecksum() {
        return checksum;
    }

    public boolean verifyChecksum(LoraFrameCodec codec) {
        return checksum == codec.computeChecksum(payloadChunk);
    }

    @Override
    public String toString() {
        return "LoraFrame{" +
                "messageId='" + messageId + '\'' +
                ", fragmentIndex=" + fragmentIndex +
                ", lastFragment=" + lastFragment +
                ", payloadLength=" + payloadChunk.length +
                ", checksum=" + Long.toHexString(checksum) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LoraFrame loraFrame)) {
            return false;
        }
        return fragmentIndex == loraFrame.fragmentIndex
                && lastFragment == loraFrame.lastFragment
                && checksum == loraFrame.checksum
                && Objects.equals(messageId, loraFrame.messageId)
                && Arrays.equals(payloadChunk, loraFrame.payloadChunk);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(messageId, fragmentIndex, lastFragment, checksum);
        result = 31 * result + Arrays.hashCode(payloadChunk);
        return result;
    }
}
