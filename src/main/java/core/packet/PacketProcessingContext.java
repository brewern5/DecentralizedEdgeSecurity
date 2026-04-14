package core.packet;

import core.connection.ConnectionManager;
import core.identity.RuntimeMembershipState;
import core.identity.TierRole;

/**
 * Immutable context object used to construct a packet manager for an incoming packet.
 *
 * <p>This class centralizes all runtime dependencies needed by manager providers so
 * handler code does not branch on packet type or rely on overloaded factory methods.
 */
public final class PacketProcessingContext {

    private final AbstractPacket receivedPacket;
    private final RuntimeMembershipState membershipState;
    private final TierRole instantiatorRole;
    private final ConnectionManager connectionManager;
    private final String senderIpAddress;

    private PacketProcessingContext(Builder builder) {
        this.receivedPacket = builder.receivedPacket;
        this.membershipState = builder.membershipState;
        this.instantiatorRole = builder.instantiatorRole;
        this.connectionManager = builder.connectionManager;
        this.senderIpAddress = builder.senderIpAddress;
    }

    public static Builder builder() {
        return new Builder();
    }

    public AbstractPacket receivedPacket() {
        return receivedPacket;
    }

    public RuntimeMembershipState membershipState() {
        return membershipState;
    }

    public TierRole instantiatorRole() {
        return instantiatorRole;
    }

    public ConnectionManager connectionManager() {
        return connectionManager;
    }

    public String senderIpAddress() {
        return senderIpAddress;
    }

    /**
     * Fluent builder for PacketProcessingContext.
     */
    public static final class Builder {
        private AbstractPacket receivedPacket;
        private RuntimeMembershipState membershipState;
        private TierRole instantiatorRole;
        private ConnectionManager connectionManager;
        private String senderIpAddress;

        private Builder() {
        }

        public Builder receivedPacket(AbstractPacket receivedPacket) {
            this.receivedPacket = receivedPacket;
            return this;
        }

        public Builder membershipState(RuntimeMembershipState membershipState) {
            this.membershipState = membershipState;
            return this;
        }

        public Builder instantiatorRole(TierRole instantiatorRole) {
            this.instantiatorRole = instantiatorRole;
            return this;
        }

        public Builder connectionManager(ConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
            return this;
        }

        public Builder senderIpAddress(String senderIpAddress) {
            this.senderIpAddress = senderIpAddress;
            return this;
        }

        public PacketProcessingContext build() {
            if (receivedPacket == null) {
                throw new IllegalArgumentException("receivedPacket cannot be null");
            }
            if (membershipState == null) {
                throw new IllegalArgumentException("membershipState cannot be null");
            }
            if (instantiatorRole == null) {
                throw new IllegalArgumentException("instantiatorRole cannot be null");
            }

            return new PacketProcessingContext(this);
        }
    }
}
