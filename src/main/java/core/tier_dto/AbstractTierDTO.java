/*
    Author: Nathniel Brewer

    This DTO is where all localized data will be held. All of this data should be immutable after instantiation.
*/  

package core.tier_dto;

public abstract class AbstractTierDTO {
    private final String name;
    private final String defaultConfigPath;
    private final String instanceConfigPath;
    private final String higherTier;
    private final String instanceId;


    /**
     * 
     * @param name The name of the tier. Ex. Node, Server, 
     * @param higherTier The tier that is above the current tier. Ex. Node's higher tier is Server, Server's is the Coordinator. Coordinator will default to network.
     * @param instanceId The ID passed as startup arguments. Ex. Node1, Server2
     */
    public AbstractTierDTO(String name, String higherTier, String instanceId) {
        this.name = name;
        this.defaultConfigPath = "config/"+name+"_config/"+name+"Config.properties";
        this.instanceId = instanceId;
        this.instanceConfigPath = "config/"+name+"_config/"+name+"Config_"+instanceId+".properties";
        this.higherTier = higherTier;
    }

    public String getName() { return name; }

    public String getDefaultConfigPath() { return defaultConfigPath; }

    public String getInstanceId() { return instanceId; }

    public String getInstanceConfigPath() { return instanceConfigPath; }

    public String getHigherTier() { return higherTier; }


}