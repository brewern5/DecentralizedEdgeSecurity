/*
    Author: Nathniel Brewer

    This DTO is where all localized data will be held. All of this data should be immutable after instantiation.
*/  

package core.identity;

import java.util.Optional;

public abstract class AbstractTierIdentity {
    private final TierRole role;
    private final TierRole higherTier;
    private final Optional<TierRole> lowerTier;

    private final String defaultConfigPath;
    private final String instanceConfigPath;
    private final String instanceId;


    /**
     * 
     * @param role The name of the tier. Ex. NODE, SERVER, COORDINATOR
     * @param higherTier The tier that is above the current tier. Ex. Node's higher tier is Server, Server's is the Coordinator. Coordinator will default to network.
     * @param instanceId The ID passed as startup arguments. Ex. Node1, Server2
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