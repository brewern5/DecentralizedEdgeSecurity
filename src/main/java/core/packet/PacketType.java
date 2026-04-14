package core.packet;

/**
 * Packet type taxonomy for all wire messages.
 *
 * <p>Request packet types are expected to resolve to a packet manager provider.
 * Response packet types are terminal in handlers and generally do not require
 * manager dispatch.
 *
 * <p>When adding a new packet type:
 * <ol>
 *   <li>Add enum value here.</li>
 *   <li>Add a packet subtype provider for Gson registration.</li>
 *   <li>Add a packet manager provider if request processing is required.</li>
 * </ol>
 */
public enum PacketType {
    INITIALIZATION,
    INITIALIZATION_RES,
    MESSAGE,
    KEEP_ALIVE,
    ERROR,
    ACK,
    PEER_LIST_REQ,
    PEER_LIST_RES
}