package components.server.identity;

import core.identity.AbstractTierIdentity;
import core.identity.TierRole;

public class ServerIdentity extends AbstractTierIdentity{
    
    /**
     * 
     * @param role The name of the tier. Ex. Node, Server, 
     * @param higherTier The tier that is above the current tier. Ex. Node's higher tier is Server, Server's is the Coordinator. Coordinator will default to network.
     * @param instanceId The ID passed as startup arguments. Ex. Node1, Server2
     */
    public ServerIdentity(TierRole role, TierRole higherTier, String instanceId) {
        super(role, higherTier, instanceId);
    }

}

