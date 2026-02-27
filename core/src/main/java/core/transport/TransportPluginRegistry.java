/*
    Author: Nathaniel Brewer

    This is the register for transport layer plugins that help determine whether or not
    added transport plugins are being recognized by the system. 
*/
package core.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class TransportPluginRegistry {

    private static final Logger logger = LogManager.getLogger(TransportPluginRegistry.class);

    private final List<TransportPlugin> plugins = new ArrayList<>();

    public TransportPluginRegistry(List<TransportPlugin> builtInPlugins) {
        if (builtInPlugins != null) {
            plugins.addAll(builtInPlugins);
        }

        ServiceLoader<TransportPlugin> loader = ServiceLoader.load(TransportPlugin.class);
        for (TransportPlugin plugin : loader) {
            plugins.add(plugin);
        }

        logger.info("Discovered {} transport plugin(s)", plugins.size());
        for (TransportPlugin plugin : plugins) {
            logger.debug("Transport plugin loaded: {}", plugin.name());
        }
    }

    public Optional<TransportPlugin> findByMode(TransportMode mode) {
        return plugins.stream().filter(plugin -> plugin.supports(mode)).findFirst();
    }
}
