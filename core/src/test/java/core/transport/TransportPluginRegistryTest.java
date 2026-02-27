package core.transport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import core.connection.ConnectionDto;

/**
 * Unit tests for {@link TransportPluginRegistry} plugin lookup behavior.
 *
 * Ensures mode-based discovery returns supported plugins and empty for unsupported modes.
 */
class TransportPluginRegistryTest {

    @Test
    void findByMode_returnsPluginWhenSupported() {
        TransportPlugin plugin = new TransportPlugin() {
            @Override
            public String name() {
                return "test-ip";
            }

            @Override
            public boolean supports(TransportMode mode) {
                return mode == TransportMode.IP;
            }

            @Override
            public TransportClient create(ConnectionDto connection) {
                return new TransportClient() {
                    @Override
                    public boolean send(core.packet.AbstractPacket packet) {
                        return true;
                    }

                    @Override
                    public boolean retry(core.packet.AbstractPacket packet) {
                        return true;
                    }

                    @Override
                    public String getAssignedId() {
                        return connection.getId();
                    }
                };
            }
        };

        TransportPluginRegistry registry = new TransportPluginRegistry(List.of(plugin));

        assertTrue(registry.findByMode(TransportMode.IP).isPresent());
        assertFalse(registry.findByMode(TransportMode.LORA).isPresent());
    }

    @Test
    void findByMode_returnsEmptyWhenNoPluginsSupportMode() {
        TransportPluginRegistry registry = new TransportPluginRegistry(List.of());
        assertFalse(registry.findByMode(TransportMode.LORA).isPresent());
    }
}
