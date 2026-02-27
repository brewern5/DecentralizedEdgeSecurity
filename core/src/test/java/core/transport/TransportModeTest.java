package core.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TransportMode} string parsing behavior.
 *
 * Validates default fallback handling and case-insensitive mode resolution.
 */
class TransportModeTest {

    @Test
    void fromString_returnsDefaultWhenInputIsNullOrBlank() {
        assertEquals(TransportMode.IP, TransportMode.fromString(null, TransportMode.IP));
        assertEquals(TransportMode.IP, TransportMode.fromString("   ", TransportMode.IP));
    }

    @Test
    void fromString_parsesCaseInsensitiveValidValue() {
        assertEquals(TransportMode.LORA, TransportMode.fromString("lora", TransportMode.IP));
        assertEquals(TransportMode.IP, TransportMode.fromString("IP", TransportMode.LORA));
    }

    @Test
    void fromString_returnsDefaultOnInvalidValue() {
        assertEquals(TransportMode.IP, TransportMode.fromString("bluetooth", TransportMode.IP));
    }
}
