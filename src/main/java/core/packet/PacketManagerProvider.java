/*
    Author: Nathaniel Brewer
*/

package core.packet;

/**
 * Service-provider contract for creating a packet manager for a specific packet type.
 *
 * <p>Implementations are discovered through ServiceLoader and can be added without
 * modifying PacketManagerFactory.
 */
public interface PacketManagerProvider {

    /**
     * @return the packet type handled by this provider.
     */
    PacketType supportsType();

    /**
     * Creates a manager for the packet represented by the provided context.
     *
     * @param context runtime context for manager construction
     * @return manager instance configured for the current packet
     */
    AbstractPacketManager create(PacketProcessingContext context);

    /**
     * Indicates whether provider requires connection context fields.
     *
     * <p>Initialization packets require both a ConnectionManager and sender IP.
     */
    default boolean requiresConnectionContext() {
        return false;
    }
}
