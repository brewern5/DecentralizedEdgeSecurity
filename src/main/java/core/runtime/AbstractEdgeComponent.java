/*
    Author: Nathaniel Brewer

    This is the main abstraction of the Component interface.
*/

package core.runtime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;

import core.config.AbstractConfig;
import core.identity.AbstractTierIdentity;
import core.identity.RuntimeMembershipState;
import core.identity.TierRole;
import core.connection.ConnectionManager;

public abstract class AbstractEdgeComponent implements EdgeComponent{
    
    protected volatile ComponentStatus status = ComponentStatus.NEW;

    protected AbstractTierIdentity identity;
    protected RuntimeMembershipState membershipState = new RuntimeMembershipState();
    protected AbstractConfig config;

    protected ScheduledExecutorService scheduler;
    protected ExecutorService listenerExecutor;

    // Will hold at least a single instance of a connection manager. Can hold multiple connection managers
    protected final Map<TierRole, ConnectionManager> connectionManagers = new ConcurrentHashMap<>();

    @Override
    public final synchronized void start(String[] args) {
        if (status == ComponentStatus.RUNNING || status == ComponentStatus.STARTING) {
            return ;
        }

        status = ComponentStatus.STARTING;
        getLogger().info("Starting component.");

        try {
            validateStartupArgs(args);
            identity = buildIdentity(args);
            config = loadConfig(identity);

            scheduler = Executors.newScheduledThreadPool(timerPoolSize());
            listenerExecutor = Executors.newCachedThreadPool();

            refreshRuntimeIp();
            initializeConnections();
            wait(100);
            startListeners();
            ensureConnectionBaseline();
            scheduleTimers();
            afterStart();

            status = ComponentStatus.RUNNING;
            
        } catch (Exception e) {
            status = ComponentStatus.FAILED;
            throw new RuntimeException("Component start failed", e);
        }
    }

    @Override
    public final synchronized void stop() {
        if (status != ComponentStatus.RUNNING) {
            return;
        }
        getLogger().info("Trying graceful shutdown . . . ");
        status = ComponentStatus.STOPPING;
        try {
            beforeStop();
            stopListeners();
            stopSchedulers();
            status = ComponentStatus.STOPPED;
            getLogger().info("Successful shutdown");
        } catch (Exception e) {
            status = ComponentStatus.FAILED;
            getLogger().error("Unsuccessful shutdown attempt!", e);
            throw new RuntimeException("Component stop failed", e);
        }
    }

    @Override
    public ComponentStatus status() {
        return status;
    }

    @Override
    public boolean health() {
        return status == ComponentStatus.RUNNING;
    }

    protected int timerPoolSize() { return 2; }

    /*
            ABSTRACTION
    */

    protected abstract Logger getLogger();
    protected abstract void validateStartupArgs(String[] args);
    protected abstract AbstractTierIdentity buildIdentity(String[] args);
    protected abstract AbstractConfig loadConfig(AbstractTierIdentity identity);
    protected void refreshRuntimeIp() {
        // Optional hook for subclasses
    }
    protected abstract void initializeConnections();
    protected abstract void startListeners();
    protected abstract void scheduleTimers();

    /*
            END ABSTRACTION
    */

    protected void ensureConnectionBaseline() {
        if(connectionManagers.isEmpty()) {
            throw new IllegalStateException("No ConnectionManager initialized. initializeConnections() must register at least one manager.");
        }
    }
    
    protected void afterStart() {
        // Optional hook for subclasses
    }

    protected void beforeStop() {
        // Optional hook for subclasses
    }

    protected void stopListeners() {
        if (listenerExecutor != null) {
            listenerExecutor.shutdownNow();
        }
    }

    protected void stopSchedulers() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

}

