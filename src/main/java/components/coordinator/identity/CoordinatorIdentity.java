package components.coordinator.identity;

import java.util.Optional;

import core.identity.AbstractTierIdentity;
import core.identity.TierRole;

public class CoordinatorIdentity extends AbstractTierIdentity{
    
    /**
     * 
     * @param role The name of the tier. Ex. NODE, SERVER, 
     * @param higherTier The tier that is above the current tier. Ex. Node's higher tier is SERVER, Server's is the COORDINATOR. Coordinator will default to NETWORK.
     * @param lowerTier The lower tier below the current tier. Node will not have this parameter
     * @param instanceId The ID passed as startup arguments. Ex. Node1, Server2
     */
    public CoordinatorIdentity(TierRole role, TierRole higherTier, TierRole lowerTier, String instanceId) {
        super(role, higherTier, Optional.of(lowerTier), instanceId);
    }

}

