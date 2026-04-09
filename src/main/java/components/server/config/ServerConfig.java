/*
    Author: Nathaniel Brewer
*/

package components.server.config;

import core.config.AbstractConfig;
import core.identity.AbstractTierIdentity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerConfig extends AbstractConfig {

    private static final Logger logger = LogManager.getLogger(ServerConfig.class);
    @Override
    protected Logger getLogger() { return logger; }

    public ServerConfig(AbstractTierIdentity tieredDto) {
        super(tieredDto);
    }

}
