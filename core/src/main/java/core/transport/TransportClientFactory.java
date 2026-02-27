package core.transport;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.connection.ConnectionDto;
import core.transport.ip.IpTransportPlugin;

public final class TransportClientFactory {

    private static final Logger logger = LogManager.getLogger(TransportClientFactory.class);

    private static final String KEY_MODE = "transport.mode";
    private static final String KEY_STRICT = "transport.strictMode";
    private static final TransportMode DEFAULT_MODE = TransportMode.IP;

    private final TransportPluginRegistry registry;

    public TransportClientFactory() {
        this.registry = new TransportPluginRegistry(List.of(new IpTransportPlugin()));
    }

    public TransportClient create(ConnectionDto connection) {
        TransportMode selectedMode = resolveMode();
        boolean strictMode = resolveStrictMode();

        return registry.findByMode(selectedMode)
                .map(plugin -> plugin.create(connection))
                .orElseGet(() -> handlePluginMissing(selectedMode, strictMode, connection));
    }

    private TransportClient handlePluginMissing(TransportMode selectedMode, boolean strictMode, ConnectionDto connection) {
        String message = "No transport plugin found for mode: " + selectedMode;
        if (strictMode) {
            throw new IllegalStateException(message + " (strict mode enabled)");
        }

        logger.warn("{}. Falling back to IP mode.", message);
        return registry.findByMode(TransportMode.IP)
                .map(plugin -> plugin.create(connection))
                .orElseThrow(() -> new IllegalStateException("IP transport plugin is not available"));
    }

    private TransportMode resolveMode() {
        String raw = System.getProperty(KEY_MODE);
        if (raw == null || raw.isBlank()) {
            raw = System.getenv("TRANSPORT_MODE");
        }
        return TransportMode.fromString(raw, DEFAULT_MODE);
    }

    private boolean resolveStrictMode() {
        String raw = System.getProperty(KEY_STRICT);
        if (raw == null || raw.isBlank()) {
            raw = System.getenv("TRANSPORT_STRICT_MODE");
        }
        return Boolean.parseBoolean(raw);
    }
}
