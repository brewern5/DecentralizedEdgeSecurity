package core.transport.ip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import core.connection.ConnectionDto;
import core.connection.Priority;
import core.transport.TransportClient;
import core.transport.TransportMode;

/**
 * Unit tests for {@link IpTransportPlugin} contract behavior.
 *
 * Verifies mode support and client creation semantics for IP transport.
 */
class IpTransportPluginTest {

    @Test
    void supports_onlyIpMode() {
        IpTransportPlugin plugin = new IpTransportPlugin();

        assertTrue(plugin.supports(TransportMode.IP));
        assertFalse(plugin.supports(TransportMode.LORA));
    }

    @Test
    void create_returnsTransportClient() {
        IpTransportPlugin plugin = new IpTransportPlugin();
        ConnectionDto connection = new ConnectionDto("node-1", "127.0.0.1", 5004, Priority.CRITICAL);

        TransportClient client = plugin.create(connection);

        assertNotNull(client);
        assertEquals("ip", plugin.name());
    }
}
