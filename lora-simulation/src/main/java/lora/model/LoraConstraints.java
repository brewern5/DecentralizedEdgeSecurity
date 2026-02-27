package lora.model;

import java.util.Objects;

/**
 * Immutable representation of the constraints for the simulated LoRa link.
 * Values are interpreted as soft limits that the simulation layer will enforce before
 * delegating to the underlying transport.
 */
public final class LoraConstraints {

    private final int mtuBytes;
    private final int perPacketOverheadBytes;
    private final int uplinkBitsPerSecond;
    private final int downlinkBitsPerSecond;
    private final int baseLatencyMs;
    private final int jitterMs;
    private final double packetLossProbability;
    private final int dutyCycleWindowMs;
    private final int maxTransmissionsPerWindow;

    public LoraConstraints(
            int mtuBytes,
            int perPacketOverheadBytes,
            int uplinkBitsPerSecond,
            int downlinkBitsPerSecond,
            int baseLatencyMs,
            int jitterMs,
            double packetLossProbability,
            int dutyCycleWindowMs,
            int maxTransmissionsPerWindow) {

        this.mtuBytes = mtuBytes;
        this.perPacketOverheadBytes = perPacketOverheadBytes;
        this.uplinkBitsPerSecond = uplinkBitsPerSecond;
        this.downlinkBitsPerSecond = downlinkBitsPerSecond;
        this.baseLatencyMs = baseLatencyMs;
        this.jitterMs = jitterMs;
        this.packetLossProbability = packetLossProbability;
        this.dutyCycleWindowMs = dutyCycleWindowMs;
        this.maxTransmissionsPerWindow = maxTransmissionsPerWindow;
    }

    public int getMtuBytes() {
        return mtuBytes;
    }

    public int getPerPacketOverheadBytes() {
        return perPacketOverheadBytes;
    }

    public int getUplinkBitsPerSecond() {
        return uplinkBitsPerSecond;
    }

    public int getDownlinkBitsPerSecond() {
        return downlinkBitsPerSecond;
    }

    public int getBaseLatencyMs() {
        return baseLatencyMs;
    }

    public int getJitterMs() {
        return jitterMs;
    }

    public double getPacketLossProbability() {
        return packetLossProbability;
    }

    public int getDutyCycleWindowMs() {
        return dutyCycleWindowMs;
    }

    public int getMaxTransmissionsPerWindow() {
        return maxTransmissionsPerWindow;
    }

    public int computeEnvelopeSizeBytes(int payloadBytes) {
        return Math.max(0, payloadBytes) + perPacketOverheadBytes;
    }

    public long estimateUplinkDurationMillis(int payloadBytes) {
        int bytes = Math.max(0, payloadBytes);
        long bits = (long) bytes * 8L;
        if (uplinkBitsPerSecond <= 0) {
            return 0L;
        }
        double millis = (double) bits / (double) uplinkBitsPerSecond * 1000.0d;
        return Math.round(millis);
    }

    public long estimateDownlinkDurationMillis(int payloadBytes) {
        int bytes = Math.max(0, payloadBytes);
        long bits = (long) bytes * 8L;
        if (downlinkBitsPerSecond <= 0) {
            return 0L;
        }
        double millis = (double) bits / (double) downlinkBitsPerSecond * 1000.0d;
        return Math.round(millis);
    }

    @Override
    public String toString() {
        return "LoraConstraints{" +
                "mtuBytes=" + mtuBytes +
                ", perPacketOverheadBytes=" + perPacketOverheadBytes +
                ", uplinkBitsPerSecond=" + uplinkBitsPerSecond +
                ", downlinkBitsPerSecond=" + downlinkBitsPerSecond +
                ", baseLatencyMs=" + baseLatencyMs +
                ", jitterMs=" + jitterMs +
                ", packetLossProbability=" + packetLossProbability +
                ", dutyCycleWindowMs=" + dutyCycleWindowMs +
                ", maxTransmissionsPerWindow=" + maxTransmissionsPerWindow +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LoraConstraints that)) {
            return false;
        }
        return mtuBytes == that.mtuBytes
                && perPacketOverheadBytes == that.perPacketOverheadBytes
                && uplinkBitsPerSecond == that.uplinkBitsPerSecond
                && downlinkBitsPerSecond == that.downlinkBitsPerSecond
                && baseLatencyMs == that.baseLatencyMs
                && jitterMs == that.jitterMs
                && Double.compare(that.packetLossProbability, packetLossProbability) == 0
                && dutyCycleWindowMs == that.dutyCycleWindowMs
                && maxTransmissionsPerWindow == that.maxTransmissionsPerWindow;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                mtuBytes,
                perPacketOverheadBytes,
                uplinkBitsPerSecond,
                downlinkBitsPerSecond,
                baseLatencyMs,
                jitterMs,
                packetLossProbability,
                dutyCycleWindowMs,
                maxTransmissionsPerWindow);
    }
}
