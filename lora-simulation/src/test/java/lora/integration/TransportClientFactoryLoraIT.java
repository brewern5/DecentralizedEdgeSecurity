package lora.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import core.connection.ConnectionDto;
import core.connection.Priority;
import core.transport.TransportClient;
import core.transport.TransportClientFactory;
import lora.plugin.LoraTransportClient;

/**
 * Integration tests for factory resolution of LORA transport through SPI discovery.
 */
class TransportClientFactoryLoraIT {

    @AfterEach
    void clearProperties() {
        System.clearProperty("transport.mode");
        System.clearProperty("transport.strictMode");
    }

    @Test
    void create_returnsLoraTransportClient_whenLoraModeAndStrictEnabled() {
        System.setProperty("transport.mode", "LORA");
        System.setProperty("transport.strictMode", "true");

        TransportClientFactory factory = new TransportClientFactory();
        ConnectionDto connection = new ConnectionDto("node-it", "127.0.0.1", 6001, Priority.CRITICAL);

        TransportClient client = factory.create(connection);

        assertNotNull(client);
        assertEquals(LoraTransportClient.class, client.getClass());
    }
}
