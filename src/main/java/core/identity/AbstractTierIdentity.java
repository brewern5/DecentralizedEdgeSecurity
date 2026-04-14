package core.identity;

import java.util.Optional;

/**
 * Immutable identity model for a runtime tier instance.
 *
 * <p>Current implementation models topology as a linear chain with one required
 * higher tier and an optional lower tier. This reflects current coordinator-server-node
 * behavior.
 *
 * <p>For future topologies (multi-parent, mesh, additional tiers), evolve this class
 * toward collections of adjacent roles rather than singular higher/lower fields.
 */
public abstract class AbstractTierIdentity {
    private final TierRole role;
    private final TierRole higherTier;
    private final Optional<TierRole> lowerTier;

    private final String defaultConfigPath;
    private final String instanceConfigPath;
    private final String instanceId;

    /**
    * @param role role of this instance
    * @param higherTier immediate upstream role in current topology
    * @param lowerTier immediate downstream role when present
    * @param instanceId startup instance ID (for config path selection)
     */
    protected AbstractTierIdentity(TierRole role, TierRole higherTier, Optional<TierRole> lowerTier, String instanceId) {
        this.role = role;
        this.instanceId = instanceId;
        this.higherTier = higherTier;
        this.lowerTier = lowerTier == null ? Optional.empty() : lowerTier;

        this.defaultConfigPath = "config/" + role + "_config/" + role + "_config_DEFAULT_" + role + ".properties";
        this.instanceConfigPath = "config/"+role+"_config/"+role+"_config_"+instanceId+".properties";
    }

    public TierRole getRole() { return role; }

    public String getDefaultConfigPath() { return defaultConfigPath; }

    public String getInstanceId() { return instanceId; }

    public String getInstanceConfigPath() { return instanceConfigPath; }

    public TierRole getHigherTier() { return higherTier; }

    public Optional<TierRole> getLowerTier() { return lowerTier; }

    public boolean hasLowerTier() {
        return lowerTier.isPresent();
    }
}