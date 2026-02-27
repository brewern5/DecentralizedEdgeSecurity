package core.transport;

public enum TransportMode {
    IP,
    LORA;

    public static TransportMode fromString(String raw, TransportMode defaultMode) {
        if (raw == null || raw.isBlank()) {
            return defaultMode;
        }
        try {
            return TransportMode.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return defaultMode;
        }
    }
}
