/*
    Author: Nathaniel Brewer

    Transport client that applies LoRa simulation constraints before delegating
    network I/O to the existing packet sender implementation.
*/

package lora.plugin;

import java.nio.charset.StandardCharsets;
import java.util.List;

import core.packet.AbstractPacket;
import core.packet.PacketType;
import core.sender.AbstractSender;
import core.sender.PacketSender;
import core.transport.TransportClient;
import lora.model.LoraConstraints;
import lora.protocol.LoraFragmenter;
import lora.protocol.LoraFrame;
import lora.simulation.LoraSimulatedLink;

public class LoraTransportClient implements TransportClient {

    private static final int MAX_MESSAGE_RETRIES = 3;
    private static final int MAX_FRAGMENT_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 250;

    private final AbstractSender delegateSender;
    private final LoraSimulatedLink simulatedLink;
    private final LoraFragmenter fragmenter;

    private String assignedId;

    public LoraTransportClient(String ip, int port, LoraConstraints constraints) {
        this(new PacketSender(ip, port), constraints);
    }

    LoraTransportClient(AbstractSender delegateSender, LoraConstraints constraints) {
        this.delegateSender = delegateSender;
        this.simulatedLink = new LoraSimulatedLink(delegateSender, constraints);
        this.fragmenter = new LoraFragmenter(constraints);
    }

    @Override
    public boolean send(AbstractPacket packet) {
        byte[] serialized = packet.toDelimitedString().getBytes(StandardCharsets.UTF_8);
        List<LoraFrame> frames = fragmenter.fragment(packet.getPacketId(), serialized);

        for (LoraFrame frame : frames) {
            if (!transmitFrameWithRetry(frame)) {
                return false;
            }
        }

        boolean sent = delegateSender.send(packet);
        if (sent && packet.getPacketType() == PacketType.INITIALIZATION) {
            assignedId = delegateSender.getAssignedId();
        }
        return sent;
    }

    @Override
    public boolean retry(AbstractPacket packet) {
        int attempts = 0;
        while (attempts < MAX_MESSAGE_RETRIES) {
            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return false;
            }

            if (send(packet)) {
                return true;
            }
            attempts++;
        }
        return false;
    }

    private boolean transmitFrameWithRetry(LoraFrame frame) {
        int attempts = 0;
        while (attempts < MAX_FRAGMENT_RETRIES) {
            if (simulatedLink.transmitFrame(frame)) {
                return true;
            }

            attempts++;
            if (attempts >= MAX_FRAGMENT_RETRIES) {
                break;
            }

            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    @Override
    public String getAssignedId() {
        return assignedId != null ? assignedId : delegateSender.getAssignedId();
    }
}
