package lora.protocol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class LoraFrameCodecTest {

    @Test
    void encodeDecode_roundTrip_preservesFields() {
        LoraFrameCodec codec = new LoraFrameCodec();
        byte[] payload = "hello-lora".getBytes(StandardCharsets.UTF_8);

        LoraFrame input = new LoraFrame(
                "msg-1",
                2,
                false,
                payload,
                codec.computeChecksum(payload));

        String encoded = codec.encode(input);
        LoraFrame decoded = codec.decode(encoded);

        assertEquals(input.getMessageId(), decoded.getMessageId());
        assertEquals(input.getFragmentIndex(), decoded.getFragmentIndex());
        assertEquals(input.isLastFragment(), decoded.isLastFragment());
        assertEquals(input.getChecksum(), decoded.getChecksum());
        assertArrayEquals(input.getPayloadChunk(), decoded.getPayloadChunk());
    }

    @Test
    void decode_throws_whenChecksumIsInvalid() {
        LoraFrameCodec codec = new LoraFrameCodec();
        byte[] payload = "checksum".getBytes(StandardCharsets.UTF_8);
        long checksum = codec.computeChecksum(payload) + 1;

        LoraFrame input = new LoraFrame("msg-2", 0, true, payload, checksum);
        String encoded = codec.encode(input);

        assertThrows(IllegalArgumentException.class, () -> codec.decode(encoded));
    }
}
