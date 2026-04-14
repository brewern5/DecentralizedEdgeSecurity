/*
 *      Author: Nathaniel Brewer
 *
 *      Factory class for creating the appropriate PacketManager based on the received packet type.
 *      This uses the PacketType from a deserialized AbstractPacket to instantiate the correct manager.
 * 
 *      Usage:
 *          // Deserialize packet using Gson with PacketTypeAdapterFactory
 *          AbstractPacket receivedPacket = gson.fromJson(json, AbstractPacket.class);
 *          
 *          // Create appropriate manager based on packet type
 *          AbstractPacketManager manager = PacketManagerFactory.createManager(
 *              receivedPacket, 
 *              myId, 
 *              clusterId, 
 *              role,
 *              connectionManager,  // Optional, only needed for INITIALIZATION
 *              senderIpAddress     // Optional, only needed for INITIALIZATION
 *          );
 *          
 *          // Process the packet
 *          manager.recreateIncomingPacket(receivedPacket);
 *          AbstractPacket response = manager.processIncomingPacket();
 */

package core.packet;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import core.connection.ConnectionManager;
import core.identity.TierRole;
import core.packet.providers.InitalizationPacketManagerProvider;
import core.packet.providers.KeepAliveManagerProvider;
import core.packet.providers.MessagePacketManagerProvider;
import core.packet.providers.PeerListPacketManagerProvider;

/**
 * Factory for resolving packet managers from a pluggable provider registry.
 *
 * <p>Providers are loaded using ServiceLoader, allowing new packet manager
 * implementations to be added without editing this factory class.
 */
public class PacketManagerFactory {

    private static final Map<PacketType, PacketManagerProvider> PROVIDERS = loadProviders();

    private PacketManagerFactory() {
    }

    private static Map<PacketType, PacketManagerProvider> loadProviders() {
        EnumMap<PacketType, PacketManagerProvider> discovered = new EnumMap<>(PacketType.class);

        ServiceLoader<PacketManagerProvider> loader = ServiceLoader.load(PacketManagerProvider.class);
        for (PacketManagerProvider provider : loader) {
            registerProvider(discovered, provider);
        }

        // Built-in providers are a fallback for environments where service resources are not resolved.
        registerIfMissing(discovered, new InitalizationPacketManagerProvider());
        registerIfMissing(discovered, new KeepAliveManagerProvider());
        registerIfMissing(discovered, new PeerListPacketManagerProvider());
        registerIfMissing(discovered, new MessagePacketManagerProvider());

        return Collections.unmodifiableMap(discovered);
    }

    private static void registerIfMissing(Map<PacketType, PacketManagerProvider> providers, PacketManagerProvider provider) {
        providers.putIfAbsent(provider.supportsType(), provider);
    }

    private static void registerProvider(Map<PacketType, PacketManagerProvider> providers, PacketManagerProvider provider) {
        PacketManagerProvider previous = providers.put(provider.supportsType(), provider);
        if (previous != null && !previous.getClass().equals(provider.getClass())) {
            throw new IllegalStateException(
                "Duplicate PacketManagerProvider for packet type "
                    + provider.supportsType()
                    + ": " + previous.getClass().getName()
                    + " and " + provider.getClass().getName()
            );
        }
    }

    /**
     * Attempts to create a manager for the incoming packet in the supplied context.
     *
     * @param context processing context
     * @return manager if provider exists; empty for response types or unsupported packet types
     */
    public static Optional<AbstractPacketManager> tryCreateManager(PacketProcessingContext context) {
        validateContext(context);

        PacketType packetType = context.receivedPacket().getPacketType();
        PacketManagerProvider provider = PROVIDERS.get(packetType);

        if (provider == null) {
            return Optional.empty();
        }

        if (provider.requiresConnectionContext()) {
            if (context.connectionManager() == null || context.senderIpAddress() == null || context.senderIpAddress().isBlank()) {
                throw new IllegalArgumentException(
                    "Packet type " + packetType + " requires connectionManager and senderIpAddress"
                );
            }
        }

        AbstractPacketManager manager = provider.create(context);
        manager.recreateIncomingPacket(context.receivedPacket());
        return Optional.of(manager);
    }

    /**
     * Creates a manager from the provided packet-processing context.
     *
     * @param context processing context
     * @return resolved manager
     * @throws IllegalArgumentException when no provider is registered for the packet type
     */
    public static AbstractPacketManager createManager(PacketProcessingContext context) {
        return tryCreateManager(context)
            .orElseThrow(() -> new IllegalArgumentException(
                "No packet manager provider registered for packet type: "
                    + context.receivedPacket().getPacketType()
            ));
    }

    /**
     * @deprecated Use createManager(PacketProcessingContext) to avoid overload-specific branching.
     */
    @Deprecated
    public static AbstractPacketManager createManager(
            AbstractPacket receivedPacket,
            core.identity.RuntimeMembershipState membershipState,
            TierRole instantiatorRole) {
        PacketProcessingContext context = PacketProcessingContext.builder()
            .receivedPacket(receivedPacket)
            .membershipState(membershipState)
            .instantiatorRole(instantiatorRole)
            .build();

        return createManager(context);
    }

    /**
     * @deprecated Use createManager(PacketProcessingContext) to avoid overload-specific branching.
     */
    @Deprecated
    public static AbstractPacketManager createManager(
            AbstractPacket receivedPacket,
            core.identity.RuntimeMembershipState membershipState,
            TierRole instantiatorRole,
            ConnectionManager connectionManager,
            String senderIpAddress) {

        PacketProcessingContext context = PacketProcessingContext.builder()
            .receivedPacket(receivedPacket)
            .membershipState(membershipState)
            .instantiatorRole(instantiatorRole)
            .connectionManager(connectionManager)
            .senderIpAddress(senderIpAddress)
            .build();

        return createManager(context);
    }

    /**
     * Returns whether a manager provider is registered for a packet type.
     *
     * @param packetType packet type to check
     * @return true when a packet manager provider is registered
     */
    public static boolean requiresManager(PacketType packetType) {
        if (packetType == null) {
            return false;
        }

        return PROVIDERS.containsKey(packetType);
    }

    private static void validateContext(PacketProcessingContext context) {
        if (context == null) {
            throw new IllegalArgumentException("PacketProcessingContext cannot be null");
        }

        if (context.receivedPacket() == null || context.receivedPacket().getPacketType() == null) {
            throw new IllegalArgumentException("Received packet and packet type cannot be null");
        }
    }
}
