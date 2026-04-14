package core.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.Logger;

import core.connection.ConnectionManager;
import core.exception.NonDelimitedPacket;
import core.external.PacketTypeAdapterFactory;
import core.identity.AbstractTierIdentity;
import core.identity.RuntimeMembershipState;
import core.packet.AbstractPacket;
import core.packet.AbstractPacketManager;
import core.packet.PacketManagerFactory;
import core.packet.PacketProcessingContext;

/**
 * Shared socket handler flow used by coordinator, server, and node packet handlers.
 *
 * <p>This abstraction centralizes transport mechanics (read/deserialize/dispatch/respond)
 * and leaves tier-specific concerns to subclasses: logger, connection manager selection,
 * and optional response-packet side effects.
 */
public abstract class AbstractSocketPacketHandler implements Runnable {

    private static final String PACKET_DELIMITER = "||END||";

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
        .create();

    private final Socket socket;
    private final AbstractTierIdentity identity;
    private final RuntimeMembershipState membershipState;

    protected AbstractSocketPacketHandler(Socket socket, AbstractTierIdentity identity, RuntimeMembershipState membershipState) {
        this.socket = socket;
        this.identity = identity;
        this.membershipState = membershipState;
    }

    protected abstract Logger getLogger();

    /**
     * @return connection manager used when handling INITIALIZATION packet registration.
     */
    protected abstract ConnectionManager getInitializationConnectionManager();

    /**
     * @return log label for remote connection type (Node, Server, Coordinator, Peer).
     */
    protected abstract String remoteTypeLabel();

    /**
     * Hook for response packets that do not require packet managers.
     */
    protected void onResponsePacket(AbstractPacket responsePacket) {
        getLogger().warn("Received response packet type: {}", responsePacket.getPacketType());
    }

    protected RuntimeMembershipState membershipState() {
        return membershipState;
    }

    protected AbstractTierIdentity identity() {
        return identity;
    }

    @Override
    public final void run() {
        getLogger().info(
            "{} connected: {}:{}",
            remoteTypeLabel(),
            socket.getInetAddress(),
            socket.getPort()
        );

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String payload = reader.readLine();
            if (payload == null || payload.isBlank()) {
                getLogger().warn("Received empty payload from {}", socket.getInetAddress());
                return;
            }

            String json = stripDelimiter(payload);
            AbstractPacket incomingPacket = GSON.fromJson(json, AbstractPacket.class);

            PacketProcessingContext context = PacketProcessingContext.builder()
                .receivedPacket(incomingPacket)
                .membershipState(membershipState)
                .instantiatorRole(identity.getRole())
                .connectionManager(getInitializationConnectionManager())
                .senderIpAddress(socket.getInetAddress().getHostAddress())
                .build();

            Optional<AbstractPacketManager> manager = PacketManagerFactory.tryCreateManager(context);
            if (manager.isEmpty()) {
                onResponsePacket(incomingPacket);
                return;
            }

            AbstractPacket responsePacket = manager.get().processIncomingPacket();
            if (responsePacket == null) {
                getLogger().warn("No response packet produced for incoming type: {}", incomingPacket.getPacketType());
                return;
            }

            respond(responsePacket);
        } catch (NonDelimitedPacket e) {
            getLogger().error("Received non-delimited packet", e);
        } catch (IllegalArgumentException e) {
            getLogger().error("Failed to process packet", e);
        } catch (IOException e) {
            getLogger().error("I/O error while handling packet", e);
        } catch (Exception e) {
            getLogger().error("Unhandled exception in socket packet handler", e);
        } finally {
            closeQuietly(reader);
            closeQuietly(socket);
        }
    }

    private String stripDelimiter(String rawPayload) throws NonDelimitedPacket {
        if (!rawPayload.endsWith(PACKET_DELIMITER)) {
            throw new NonDelimitedPacket("Received packet does not end with delimiter \"||END||\".");
        }

        return rawPayload.substring(0, rawPayload.length() - PACKET_DELIMITER.length());
    }

    private void respond(AbstractPacket responsePacket) throws IOException {
        try (PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {
            output.println(responsePacket.toDelimitedString());
            output.flush();
        }
    }

    private void closeQuietly(BufferedReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                getLogger().warn("Failed closing socket reader", e);
            }
        }
    }

    private void closeQuietly(Socket socketToClose) {
        if (socketToClose != null && !socketToClose.isClosed()) {
            try {
                socketToClose.close();
            } catch (IOException e) {
                getLogger().warn("Failed closing socket", e);
            }
        }
    }
}
