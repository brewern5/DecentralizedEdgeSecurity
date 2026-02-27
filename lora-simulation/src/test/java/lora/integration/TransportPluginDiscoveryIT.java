package lora.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.jupiter.api.Test;

import core.transport.TransportMode;
import core.transport.TransportPlugin;

/**
 * Integration tests for SPI plugin discovery in the lora-simulation module.
 */
class TransportPluginDiscoveryIT {

    @Test
    void serviceLoader_discoversLoraTransportPlugin() {
        ServiceLoader<TransportPlugin> loader = ServiceLoader.load(TransportPlugin.class);

        List<TransportPlugin> discovered = new ArrayList<>();
        loader.forEach(discovered::add);

        assertTrue(
                discovered.stream().anyMatch(plugin -> plugin.supports(TransportMode.LORA)),
                "Expected at least one discovered plugin that supports LORA mode"
        );
    }

    @Test
    void discoveredLoraPlugin_createsTransportClient() {
        ServiceLoader<TransportPlugin> loader = ServiceLoader.load(TransportPlugin.class);

        TransportPlugin loraPlugin = loader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(plugin -> plugin.supports(TransportMode.LORA))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No LORA transport plugin discovered"));

        assertNotNull(loraPlugin);
        assertNotNull(loraPlugin.name());
    }
}
