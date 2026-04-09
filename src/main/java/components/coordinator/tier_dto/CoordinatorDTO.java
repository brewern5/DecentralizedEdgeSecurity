package components.coordinator.tier_dto;

import core.tier_dto.AbstractTierDTO;

public class CoordinatorDTO extends AbstractTierDTO{
    
    /**
     * 
     * @param name The name of the tier. Ex. Node, Server, 
     * @param higherTier The tier that is above the current tier. Ex. Node's higher tier is Server, Server's is the Coordinator. Coordinator will default to network.
     * @param instanceId The ID passed as startup arguments. Ex. Node1, Server2
     */
    public CoordinatorDTO(String name, String higherTier, String instanceId) {
        super(name, higherTier, instanceId);
    }

}

