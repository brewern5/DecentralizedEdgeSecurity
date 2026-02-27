/*
    Author: Nathaniel Brewer

    Applies LoRa-like constraints (MTU, throughput, latency, packet loss, duty cycle)
    before delegating packets to the underlying transport.
*/

package lora.simulation;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.packet.AbstractPacket;
import core.sender.AbstractSender;
import core.sender.PacketSender;
import lora.model.LoraConstraints;
import lora.protocol.LoraFrame;
import lora.protocol.LoraFrameCodec;

public class LoraSimulatedLink {

    private static final Logger logger = LogManager.getLogger(LoraSimulatedLink.class);

    private final LoraConstraints constraints;
    private final AbstractSender delegate;
    private final LoraFrameCodec frameCodec;

    private Instant windowStart = Instant.now();
    private int windowTransmissions = 0;

    public LoraSimulatedLink(String ip, int port, LoraConstraints constraints) {
        this(new PacketSender(ip, port), constraints);
    }

    public LoraSimulatedLink(AbstractSender delegate, LoraConstraints constraints) {
        this.delegate = delegate;
        this.constraints = constraints;
        this.frameCodec = new LoraFrameCodec();
    }

    /**
     * Sends a packet after simulating LoRa constraints. Returns {@code false} if the packet
     * violates MTU, is dropped due to loss simulation, or the delegate fails to send.
     */
    public synchronized boolean send(AbstractPacket packet) {
        byte[] serialized = packet.toDelimitedString().getBytes(StandardCharsets.UTF_8);
        if (!simulateTransmission(serialized, "packet " + packet.getPacketId())) {
            return false;
        }

        boolean sent = delegate.send(packet);
        if (sent) {
            windowTransmissions++;
        }
        return sent;
    }

    public synchronized boolean transmitFrame(LoraFrame frame) {
        byte[] encoded = frameCodec.encode(frame).getBytes(StandardCharsets.UTF_8);
        boolean transmitted = simulateTransmission(
                encoded,
                "frame " + frame.getMessageId() + "#" + frame.getFragmentIndex());
        if (transmitted) {
            windowTransmissions++;
        }
        return transmitted;
    }

    private boolean simulateTransmission(byte[] payload, String transmissionLabel) {
        int envelopeSize = constraints.computeEnvelopeSizeBytes(payload.length);

        if (envelopeSize > constraints.getMtuBytes()) {
            logger.warn("{} exceeds LoRa MTU ({} bytes > {} bytes). Dropping.",
                    transmissionLabel, envelopeSize, constraints.getMtuBytes());
            return false;
        }

        long uplinkDurationMs = constraints.estimateUplinkDurationMillis(envelopeSize);
        long latencyBudget = computeLatencyBudget();

        enforceDutyCycle();

        if (shouldDrop()) {
            logger.info("Simulated loss for {}", transmissionLabel);
            return false;
        }

        sleepQuietly(uplinkDurationMs + latencyBudget);
        return true;
    }

    private long computeLatencyBudget() {
        int jitter = Math.max(0, constraints.getJitterMs());
        int jitterContribution = jitter == 0
                ? 0
                : ThreadLocalRandom.current().nextInt(jitter + 1);
        return constraints.getBaseLatencyMs() + jitterContribution;
    }

    private boolean shouldDrop() {
        double probability = constraints.getPacketLossProbability();
        if (probability <= 0.0d) {
            return false;
        }
        double draw = ThreadLocalRandom.current().nextDouble();
        return draw < probability;
    }

    private void enforceDutyCycle() {
        long windowMillis = constraints.getDutyCycleWindowMs();
        if (windowMillis <= 0) {
            return; // Duty cycle disabled
        }

        Instant now = Instant.now();
        Duration sinceWindowStart = Duration.between(windowStart, now);
        if (sinceWindowStart.toMillis() >= windowMillis) {
            windowStart = now;
            windowTransmissions = 0;
            return;
        }

        if (windowTransmissions >= constraints.getMaxTransmissionsPerWindow()) {
            long remaining = windowMillis - sinceWindowStart.toMillis();
            logger.info("Duty cycle window full ({} transmissions). Sleeping {} ms to reset window.",
                    windowTransmissions, remaining);
            sleepQuietly(remaining);
            windowStart = Instant.now();
            windowTransmissions = 0;
        }
    }

    private void sleepQuietly(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while simulating LoRa constraints.");
        }
    }
}
