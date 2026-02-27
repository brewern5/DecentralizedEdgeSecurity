package core.transport;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import core.connection.ConnectionDto;
import core.connection.Priority;

/**
 * Unit tests for {@link TransportClientFactory} mode selection behavior.
 *
 * Covers default IP behavior, fallback when strict mode is disabled, and strict failure mode.
 */
class TransportClientFactoryTest {

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty("transport.mode");
        System.clearProperty("transport.strictMode");
    }

    @Test
    void create_defaultsToIpWhenModeMissing() {
        TransportClientFactory factory = new TransportClientFactory();
        ConnectionDto connection = new ConnectionDto("server-1", "127.0.0.1", 5003, Priority.CRITICAL);

        TransportClient client = factory.create(connection);

        assertNotNull(client);
    }

    @Test
    void create_fallsBackToIpWhenModeUnsupportedAndStrictFalse() {
        System.setProperty("transport.mode", "LORA");
        System.setProperty("transport.strictMode", "false");

        TransportClientFactory factory = new TransportClientFactory();
        ConnectionDto connection = new ConnectionDto("server-2", "127.0.0.1", 5003, Priority.CRITICAL);

        TransportClient client = factory.create(connection);

        assertNotNull(client);
    }

    @Test
    void create_throwsWhenModeUnsupportedAndStrictTrue() {
        System.setProperty("transport.mode", "LORA");
        System.setProperty("transport.strictMode", "true");

        TransportClientFactory factory = new TransportClientFactory();
        ConnectionDto connection = new ConnectionDto("server-3", "127.0.0.1", 5003, Priority.CRITICAL);

        assertThrows(IllegalStateException.class, () -> factory.create(connection));
    }
}
