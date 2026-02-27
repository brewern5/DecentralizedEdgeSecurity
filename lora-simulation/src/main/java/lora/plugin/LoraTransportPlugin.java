/*
    Author: Nathaniel Brewer

    ServiceLoader-discoverable plugin that provides LORA transport clients.
*/
package lora.plugin;

import core.connection.ConnectionDto;
import core.transport.TransportClient;
import core.transport.TransportMode;
import core.transport.TransportPlugin;
import lora.config.LoraSimulationConfig;
import lora.model.LoraConstraints;

public class LoraTransportPlugin implements TransportPlugin {

    @Override
    public String name() {
        return "lora-simulation";
    }

    @Override
    public boolean supports(TransportMode mode) {
        return mode == TransportMode.LORA;
    }

    @Override
    public TransportClient create(ConnectionDto connection) {
        LoraConstraints constraints = new LoraSimulationConfig().toConstraints();
        return new LoraTransportClient(connection.getIp(), connection.getPort(), constraints);
    }
}
