# Edge Refactor Blueprint (Updated)

This document captures the updated refactor direction:

1. Use one global parent abstraction for shared lifecycle.
2. Keep one concrete component per tier.
3. Add tier-specific abstract classes only when multiple implementations of the same tier appear.

No production source files are changed by this blueprint.

## 1) Target Design

### Core lifecycle abstractions

- `EdgeComponent` (interface): lifecycle contract
- `AbstractEdgeComponent` (single parent class): startup and shutdown template
- `NodeComponent`, `ServerComponent`, `CoordinatorComponent` (concrete classes): tier behavior

### Supporting model abstractions

- `TierRole` (enum): `NODE`, `SERVER`, `COORDINATOR`, `NETWORK`
- `TierIdentity` (immutable class): role, instanceId, higherTier, config paths
- `RuntimeMembershipState` (mutable class): assignedId, clusterId
- `ComponentStatus` (enum): `NEW`, `STARTING`, `RUNNING`, `STOPPING`, `STOPPED`, `FAILED`

### Composition helpers (preferred over deeper inheritance)

- `BootstrapStrategy`: connection bootstrap per tier
- `ListenerPlan`: listener definitions per tier
- `TimerPlan`: keep-alive and expiry task definitions
- `KeepAlivePolicy`: failure and retry policy per connection type

## 2) Java Skeletons

### 2.1 EdgeComponent

```java
package core.runtime;

public interface EdgeComponent {
    void start(String[] args);
    void stop();
    ComponentStatus status();
    boolean health();
}
```

### 2.2 ComponentStatus

```java
package core.runtime;

public enum ComponentStatus {
    NEW,
    STARTING,
    RUNNING,
    STOPPING,
    STOPPED,
    FAILED
}
```

### 2.3 TierRole

```java
package core.identity;

public enum TierRole {
    NODE,
    SERVER,
    COORDINATOR,
    NETWORK
}
```

### 2.4 TierIdentity

```java
package core.identity;

public final class TierIdentity {

    private final TierRole role;
    private final String instanceId;
    private final String higherTier;
    private final String defaultConfigPath;
    private final String instanceConfigPath;

    public TierIdentity(TierRole role, String instanceId, String higherTier) {
        String roleName = role.name().toLowerCase();
        this.role = role;
        this.instanceId = instanceId;
        this.higherTier = higherTier;
        this.defaultConfigPath = "config/" + roleName + "_config/" + roleName + "Config.properties";
        this.instanceConfigPath = "config/" + roleName + "_config/" + roleName + "Config_" + instanceId + ".properties";
    }

    public TierRole role() { return role; }
    public String instanceId() { return instanceId; }
    public String higherTier() { return higherTier; }
    public String defaultConfigPath() { return defaultConfigPath; }
    public String instanceConfigPath() { return instanceConfigPath; }
}
```

### 2.5 RuntimeMembershipState

```java
package core.identity;

public final class RuntimeMembershipState {

    private volatile String assignedId;
    private volatile String clusterId;

    public String assignedId() { return assignedId; }
    public void assignId(String assignedId) { this.assignedId = assignedId; }

    public String clusterId() { return clusterId; }
    public void assignClusterId(String clusterId) { this.clusterId = clusterId; }
}
```

### 2.6 AbstractEdgeComponent

```java
package core.runtime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import core.config.AbstractConfig;
import core.identity.TierIdentity;
import core.identity.RuntimeMembershipState;

public abstract class AbstractEdgeComponent implements EdgeComponent {

    protected volatile ComponentStatus status = ComponentStatus.NEW;

    protected TierIdentity identity;
    protected RuntimeMembershipState membershipState = new RuntimeMembershipState();
    protected AbstractConfig config;

    protected ScheduledExecutorService scheduler;
    protected ExecutorService listenerExecutor;

    @Override
    public final synchronized void start(String[] args) {
        if (status == ComponentStatus.RUNNING || status == ComponentStatus.STARTING) {
            return;
        }

        status = ComponentStatus.STARTING;

        try {
            validateStartupArgs(args);
            identity = buildIdentity(args);
            config = loadConfig(identity);

            scheduler = Executors.newScheduledThreadPool(timerPoolSize());
            listenerExecutor = Executors.newCachedThreadPool();

            initializeConnections();
            startListeners();
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

        status = ComponentStatus.STOPPING;
        try {
            beforeStop();
            stopListeners();
            stopSchedulers();
            status = ComponentStatus.STOPPED;
        } catch (Exception e) {
            status = ComponentStatus.FAILED;
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

    protected abstract void validateStartupArgs(String[] args);
    protected abstract TierIdentity buildIdentity(String[] args);
    protected abstract AbstractConfig loadConfig(TierIdentity identity);
    protected abstract void initializeConnections();
    protected abstract void startListeners();
    protected abstract void scheduleTimers();

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
```

### 2.7 NodeComponent

```java
package components.node.runtime;

import core.config.AbstractConfig;
import core.identity.TierIdentity;
import core.identity.TierRole;
import core.runtime.AbstractEdgeComponent;

public final class NodeComponent extends AbstractEdgeComponent {

    @Override
    protected void validateStartupArgs(String[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            throw new IllegalArgumentException("Node requires instanceId argument");
        }
    }

    @Override
    protected TierIdentity buildIdentity(String[] args) {
        return new TierIdentity(TierRole.NODE, args[0], "Server");
    }

    @Override
    protected AbstractConfig loadConfig(TierIdentity identity) {
        // return new NodeConfig(new NodeDTO("node", "Server", identity.instanceId()));
        return null;
    }

    @Override
    protected void initializeConnections() {
        // Node -> Server bootstrap and initialization packet
        // Then update membershipState.assignId(...) and membershipState.assignClusterId(...)
    }

    @Override
    protected void startListeners() {
        // Start server listener and optional peer listener
    }

    @Override
    protected void scheduleTimers() {
        // Schedule keep-alive to server
    }

    @Override
    protected void afterStart() {
        // Optional peer list request
    }
}
```

### 2.8 ServerComponent

```java
package components.server.runtime;

import core.config.AbstractConfig;
import core.identity.TierIdentity;
import core.identity.TierRole;
import core.runtime.AbstractEdgeComponent;

public final class ServerComponent extends AbstractEdgeComponent {

    @Override
    protected void validateStartupArgs(String[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            throw new IllegalArgumentException("Server requires instanceId argument");
        }
    }

    @Override
    protected TierIdentity buildIdentity(String[] args) {
        return new TierIdentity(TierRole.SERVER, args[0], "Coordinator");
    }

    @Override
    protected AbstractConfig loadConfig(TierIdentity identity) {
        // return new ServerConfig(new ServerDTO("server", "Coordinator", identity.instanceId()));
        return null;
    }

    @Override
    protected void initializeConnections() {
        // Server -> Coordinator bootstrap
        // Then update membershipState.assignId(...) and membershipState.assignClusterId(...)
    }

    @Override
    protected void startListeners() {
        // Start coordinator listener and node listener
    }

    @Override
    protected void scheduleTimers() {
        // Keep-alive to coordinator + expiry checks for node connections
    }
}
```

### 2.9 CoordinatorComponent

```java
package components.coordinator.runtime;

import core.config.AbstractConfig;
import core.identity.TierIdentity;
import core.identity.TierRole;
import core.runtime.AbstractEdgeComponent;

public final class CoordinatorComponent extends AbstractEdgeComponent {

    @Override
    protected void validateStartupArgs(String[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            throw new IllegalArgumentException("Coordinator requires instanceId argument");
        }
    }

    @Override
    protected TierIdentity buildIdentity(String[] args) {
        return new TierIdentity(TierRole.COORDINATOR, args[0], "Network");
    }

    @Override
    protected AbstractConfig loadConfig(TierIdentity identity) {
        // return new CoordinatorConfig(new CoordinatorDTO("coordinator", "Network", identity.instanceId()));
        return null;
    }

    @Override
    protected void initializeConnections() {
        // Initialize coordinator connection manager
        // If assigned by upstream, update membershipState.assignId(...)
    }

    @Override
    protected void startListeners() {
        // Start server listener
    }

    @Override
    protected void scheduleTimers() {
        // Expiry checks and coordinator background tasks
    }
}
```

## 3) Migration Plan

### Phase 1: Introduce lifecycle scaffolding only

1. Add `EdgeComponent`, `ComponentStatus`, `AbstractEdgeComponent`, `TierRole`, `TierIdentity`.
2. Keep all existing entry-point classes untouched.
3. Verify build still passes.

### Phase 2: Move one tier first (Node)

1. Implement `NodeComponent` using existing Node logic.
2. Keep `EdgeNode.main` as a thin launcher that delegates to `NodeComponent.start`.
3. Verify startup and keep-alive behavior are unchanged.

### Phase 3: Move Server and Coordinator

1. Implement `ServerComponent` and `CoordinatorComponent`.
2. Keep old mains as compatibility launchers.
3. Verify listener ports, initialization flow, and keep-alive behavior.

### Phase 4: Optional cleanup

1. Extract shared listener and handler pipelines.
2. Consolidate duplicated keep-alive connection manager behavior behind policy classes.
3. Remove legacy static fields where no longer needed.

## 4) Why this version is simpler

- One parent abstraction controls lifecycle everywhere.
- No extra per-tier abstract classes until there is proven need.
- Tier differences stay in concrete classes and composition helpers.
- New tiers can be added without touching existing tier internals.
