/*
    Author: Nathaniel Brewer
*/

package components.node.config;

import core.config.AbstractConfig;
import core.tier_dto.AbstractTierDTO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NodeConfig extends AbstractConfig {

    private static final Logger logger = LogManager.getLogger(NodeConfig.class);
    @Override
    protected Logger getLogger() { return logger; }

    public NodeConfig(AbstractTierDTO tieredDto) {
        super(tieredDto);
    }

}
