package core.identity;

/**
 * Role model for runtime tiers in the current network topology.
 *
 * <p>Current implementation assumes a coordinator-server-node hierarchy.
 * Additional roles can be introduced in future topologies, but component,
 * listener, and connection wiring must be updated accordingly.
 */
public enum TierRole {
    NODE,
    SERVER,
    COORDINATOR,
    NETWORK // Placeholder for external network connections where coordinator acts as gateway
}
