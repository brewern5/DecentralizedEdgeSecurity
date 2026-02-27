/*
    Author: Nathaniel Brewer

    Encodes and decodes singular LoRa frames to a compact transport string representation.
    Each frame will have their own metadata and checksum for error handling
*/

package lora.protocol;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.CRC32;

public final class LoraFrameCodec {

    private static final String VERSION = "LORA1";
    private static final String SEPARATOR = "|";
    private static final int PARTS = 6;

    public String encode(LoraFrame frame) {
        String payloadBase64 = Base64.getEncoder().encodeToString(frame.getPayloadChunk());
        String isLast = frame.isLastFragment() ? "1" : "0";
        String checksumHex = Long.toHexString(frame.getChecksum());

        return String.join(
                SEPARATOR,
                VERSION,
                frame.getMessageId(),
                Integer.toString(frame.getFragmentIndex()),
                isLast,
                payloadBase64,
                checksumHex
        );
    }

    public LoraFrame decode(String encodedFrame) {
        if (encodedFrame == null || encodedFrame.isBlank()) {
            throw new IllegalArgumentException("encodedFrame must not be blank");
        }

        String[] parts = encodedFrame.split("\\|", PARTS);
        if (parts.length != PARTS) {
            throw new IllegalArgumentException("Invalid frame format");
        }
        if (!VERSION.equals(parts[0])) {
            throw new IllegalArgumentException("Unsupported frame version: " + parts[0]);
        }

        String messageId = parts[1];
        int fragmentIndex = parseInt(parts[2], "fragmentIndex");
        boolean lastFragment = parseLast(parts[3]);

        byte[] payloadChunk;
        try {
            payloadChunk = Base64.getDecoder().decode(parts[4]);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid base64 payload", ex);
        }

        long checksum;
        try {
            checksum = Long.parseUnsignedLong(parts[5], 16);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid checksum", ex);
        }

        LoraFrame frame = new LoraFrame(messageId, fragmentIndex, lastFragment, payloadChunk, checksum);
        if (!frame.verifyChecksum(this)) {
            throw new IllegalArgumentException("Checksum mismatch for frame " + messageId + "#" + fragmentIndex);
        }

        return frame;
    }

    public long computeChecksum(byte[] payload) {
        CRC32 crc32 = new CRC32();
        crc32.update(payload);
        return crc32.getValue();
    }

    public int utf8Length(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }

    private int parseInt(String raw, String fieldName) {
        try {
            int value = Integer.parseInt(raw);
            if (value < 0) {
                throw new IllegalArgumentException(fieldName + " must be >= 0");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + fieldName, ex);
        }
    }

    private boolean parseLast(String raw) {
        if ("1".equals(raw)) {
            return true;
        }
        if ("0".equals(raw)) {
            return false;
        }
        throw new IllegalArgumentException("Invalid last fragment flag: " + raw);
    }
}
