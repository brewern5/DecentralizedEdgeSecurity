/*
    Author: Nathaniel Brewer

    Loads configuration for the LoRa simulation transport constraints.

    Default path: ./config/lora/lora_simulation/loraConfig.properties

    Any new simulation (testing best case/worst case) will be added as a .properties
*/
package lora.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lora.model.LoraConstraints;

/**
 * Loads LoRa simulation constraints from a properties file. The loader prefers an explicit
 * path provided via the {@code lora.config.path} system property or {@code LORA_CONFIG_PATH}
 * environment variable. If neither is set, it falls back to {@value #DEFAULT_CONFIG_PATH}.
 */
public final class LoraSimulationConfig {

    private static final Logger logger = LogManager.getLogger(LoraSimulationConfig.class);

    private static final String DEFAULT_CONFIG_PATH = "config/lora/lora_simulation/loraConfig.properties";

    private static final String KEY_MTU = "lora.mtuBytes";
    private static final String KEY_OVERHEAD = "lora.perPacketOverheadBytes";
    private static final String KEY_UPLINK_BPS = "lora.uplinkBitsPerSecond";
    private static final String KEY_DOWNLINK_BPS = "lora.downlinkBitsPerSecond";
    private static final String KEY_LATENCY_MS = "lora.latencyMs";
    private static final String KEY_JITTER_MS = "lora.jitterMs";
    private static final String KEY_PACKET_LOSS = "lora.packetLossProbability";
    private static final String KEY_DUTY_WINDOW_MS = "lora.dutyCycleWindowMs";
    private static final String KEY_MAX_TX_WINDOW = "lora.maxTransmissionsPerWindow";

    private static final int DEFAULT_MTU_BYTES = 51;
    private static final int DEFAULT_OVERHEAD_BYTES = 13;
    private static final int DEFAULT_UPLINK_BPS = 5_000;
    private static final int DEFAULT_DOWNLINK_BPS = 5_000;
    private static final int DEFAULT_LATENCY_MS = 250;
    private static final int DEFAULT_JITTER_MS = 50;
    private static final double DEFAULT_PACKET_LOSS = 0.02d;
    private static final int DEFAULT_DUTY_WINDOW_MS = 60_000;
    private static final int DEFAULT_MAX_TX = 30;

    private final Path configPath;
    private final Properties properties = new Properties();

    public LoraSimulationConfig() {
        this(resolveConfigPath());
    }

    public LoraSimulationConfig(Path configPath) {
        this.configPath = configPath;
        load();
    }

    public LoraConstraints toConstraints() {
        int mtu = readInt(KEY_MTU, DEFAULT_MTU_BYTES);
        int overhead = readInt(KEY_OVERHEAD, DEFAULT_OVERHEAD_BYTES);
        int uplink = readInt(KEY_UPLINK_BPS, DEFAULT_UPLINK_BPS);
        int downlink = readInt(KEY_DOWNLINK_BPS, DEFAULT_DOWNLINK_BPS);
        int latency = readInt(KEY_LATENCY_MS, DEFAULT_LATENCY_MS);
        int jitter = readInt(KEY_JITTER_MS, DEFAULT_JITTER_MS);
        double packetLoss = readDouble(KEY_PACKET_LOSS, DEFAULT_PACKET_LOSS);
        int dutyWindow = readInt(KEY_DUTY_WINDOW_MS, DEFAULT_DUTY_WINDOW_MS);
        int maxTxWindow = readInt(KEY_MAX_TX_WINDOW, DEFAULT_MAX_TX);

        return new LoraConstraints(
                mtu,
                overhead,
                uplink,
                downlink,
                latency,
                jitter,
                packetLoss,
                dutyWindow,
                maxTxWindow
        );
    }

    private void load() {
        if (!Files.exists(configPath)) {
            logger.warn("LoRa config file not found at {}. Using defaults.", configPath.toAbsolutePath());
            return;
        }
        try (FileInputStream in = new FileInputStream(configPath.toFile())) {
            properties.load(in);
            logger.info("Loaded LoRa config from {}", configPath.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed reading LoRa config from {}. Falling back to defaults.", configPath.toAbsolutePath(), e);
        }
    }

    private int readInt(String key, int defaultValue) {
        String raw = properties.getProperty(key);
        if (raw == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            logger.warn("Invalid integer for {}='{}'. Using default {}.", key, raw, defaultValue);
            return defaultValue;
        }
    }

    private double readDouble(String key, double defaultValue) {
        String raw = properties.getProperty(key);
        if (raw == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ex) {
            logger.warn("Invalid decimal for {}='{}'. Using default {}.", key, raw, defaultValue);
            return defaultValue;
        }
    }

    private static Path resolveConfigPath() {
        String sysProp = System.getProperty("lora.config.path");
        if (sysProp != null && !sysProp.isBlank()) {
            return Paths.get(sysProp.trim());
        }
        String env = System.getenv("LORA_CONFIG_PATH");
        if (env != null && !env.isBlank()) {
            return Paths.get(env.trim());
        }
        return Paths.get(DEFAULT_CONFIG_PATH);
    }
}
