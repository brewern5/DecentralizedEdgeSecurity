package components.coordinator.runtime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import components.coordinator.identity.CoordinatorIdentity;
import components.coordinator.listener.CoordinatorListener;

import components.server.connections.ServerNodeConnectionManager;

import components.coordinator.config.CoordinatorConfig;

import components.coordinator.connections.CoordinatorConnectionManager;

import core.config.AbstractConfig;

import core.connection.ConnectionManager;

import core.identity.AbstractTierIdentity;
import core.identity.TierRole;

import core.runtime.AbstractEdgeComponent;

public class CoordinatorComponent extends AbstractEdgeComponent {
    
    private static CoordinatorListener coordinatorListener;

    private static final Logger logger = LogManager.getLogger(CoordinatorComponent.class);
    @Override
    protected Logger getLogger() { return logger; }

    @Override
    protected void validateStartupArgs(String args[]) {
        if (args == null || args.length == 0 || args[0] == null) {
            logger.error("Server requires instanceId argument!");
            throw new IllegalArgumentException("Server requires instanceId argument");
        }
    }

    @Override
    protected AbstractTierIdentity buildIdentity(String[] args) {
        membershipState.assignClusterId(UUID.randomUUID().toString());
        membershipState.assignId(UUID.randomUUID().toString());

        return new CoordinatorIdentity(TierRole.COORDINATOR, TierRole.NETWORK, TierRole.SERVER, args[0]);
    }

    @Override 
    protected AbstractConfig loadConfig(AbstractTierIdentity identity) {
        return new CoordinatorConfig(identity);
    }

    @Override
    protected void refreshRuntimeIp() {
        try{
            config.grabIP();
        } catch (SocketException se){
            logger.error("Exception thrown for socket creations!", se);
        } catch(UnknownHostException uhe) {
            logger.error("Could not determine machine IP!", uhe);
        }
    }

    @Override
    protected void initializeConnections() {

        try {
            connectionManagers.put(
                identity.getHigherTier(), 
                CoordinatorConnectionManager.getInstance(membershipState, identity.getRole())
            );
            
        } catch(Exception e) {
            logger.error("Error Initalizing Connection Manager: ", e);
        }

        try{

            TierRole lowerTier = identity.getLowerTier()
                .orElseThrow(() -> new IllegalStateException("Coordinator must have a lower tier"));


            connectionManagers.put(
                lowerTier, 
                ServerNodeConnectionManager.getInstance(
                    membershipState,
                    identity.getRole()
                )
            );
        } catch (Exception e){
            logger.error("Unhandled Exception in Node Connection manager creation!");
        }

    }

    @Override
    protected void startListeners() {

        int timeoutMs = 2000; 

        try {
            coordinatorListener = new CoordinatorListener(
                config.getPortByKey("Coordinator.listeningPort"), 
                timeoutMs,
                identity,
                membershipState
            );

            listenerExecutor.execute(coordinatorListener);
            logger.info("Coordinator listener submitted on port {}", coordinatorListener.getActivePort());
        } catch (Exception e) {
            logger.error(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("Coordinator.listeningPort")
                + e
            );
            // TODO: try to grab new port if this one is unavailable
        }
    }

    @Override
    protected void scheduleTimers() {
        try{
            if (scheduler == null) {
                throw new IllegalStateException("Scheduler is not initialized before scheduleTimers()");
            }

            scheduler.scheduleAtFixedRate(() ->{
                TierRole lowerTier = identity.getLowerTier()
                    .orElseThrow(() -> new IllegalStateException("Coordinator must have a lower tier"));

                ConnectionManager lowerTierManager = connectionManagers.get(lowerTier);
                if (lowerTierManager == null) {
                    logger.error("No ConnectionManager registered for lower tier: {}", lowerTier);
                    return;
                }

                lowerTierManager.checkExpiredConnections();

            }, 5, 30, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Unhandled Exception when creating Scheduler!", e);
        }
        
    }

    @Override
    protected void beforeStop() {
        if (coordinatorListener != null) {
            coordinatorListener.closeSocket();
            logger.warn("Coordinator listener socket closed!");
        }
    }

    @Override
    protected void afterStart() { return; }

}
