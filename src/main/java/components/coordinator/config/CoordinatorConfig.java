/*
    Author: Nathaniel Brewer
*/

package components.coordinator.config;

import core.config.AbstractConfig;
import core.identity.AbstractTierIdentity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CoordinatorConfig extends AbstractConfig {

    private static final Logger logger = LogManager.getLogger(CoordinatorConfig.class);
    @Override
    protected Logger getLogger() { return logger; }

    public CoordinatorConfig(AbstractTierIdentity tieredDto) {
        super(tieredDto);
    }

}
