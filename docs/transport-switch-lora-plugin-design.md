# Feature Design: Transport Switch Interface (IP ↔ LoRa) with LoRa Plugin Integration

- **Project:** DecentralizedEdgeSecurity
- **Status:** Proposed
- **Date:** 2026-02-27
- **Audience:** Core platform maintainers, networking contributors

---

## 1) Objective

Introduce a transport abstraction that allows runtime switching between:

- **IP mode** (existing direct TCP sender behavior), and
- **LoRa mode** (via the `lora-simulation` plugin/module).

The design must ensure the new LoRa plugin integrates through a stable interface instead of direct coupling to transport implementation details.

---

## 2) Problem Statement

Current send behavior is tightly coupled to `PacketSender` inside `ConnectionDtoManager`. Specifically:

- `ConnectionDtoManager#createSender()` always creates `new PacketSender(ip, port)`.
- Keep-alive and normal packet sends all flow through `ConnectionDtoManager#send(...)`.
- LoRa simulation exists (`LoraSimulatedLink`) but is not selected through a first-class transport interface.

Result: adding/choosing alternative transports requires modifying core send logic directly.

---

## 3) Design Goals

1. **Runtime transport selection** using configuration (`IP` or `LORA`).
2. **Plugin-based extensibility** so future transports can be added without changing core send workflow.
3. **Backward compatibility**: default behavior remains current IP sender path.
4. **Minimal operational change** to packet shape, ACK semantics, and connection lifecycle.

## 4) Non-Goals

- Rewriting packet schema.
- Replacing TCP stack internals.
- Modeling physical LoRa hardware protocol internals in core.

---

## 5) Proposed Architecture

### 5.1 Core Abstractions

Add a transport API in `core`:

- `core.transport.TransportMode` enum (`IP`, `LORA`)
- `core.transport.TransportClient` interface
- `core.transport.TransportPlugin` interface
- `core.transport.TransportPluginRegistry` using Java `ServiceLoader`
- `core.transport.TransportClientFactory` to create client by mode and connection

### 5.2 Interface Contracts

```java
public enum TransportMode {
    IP,
    LORA
}

public interface TransportClient {
    boolean send(AbstractPacket packet);
    boolean retry(AbstractPacket packet);
    String getAssignedId();
}

public interface TransportPlugin {
    String name();
    boolean supports(TransportMode mode);
    TransportClient create(ConnectionDto connection);
}
```

### 5.3 Built-in IP Plugin (core)

Add `IpTransportPlugin` in `core`:

- Wraps existing `PacketSender` behavior.
- Preserves current ACK/retry semantics.
- Serves as default/fallback when no plugin is specified.

### 5.4 LoRa Plugin (lora-simulation)

Add `LoraTransportPlugin` in `lora-simulation`:

- Loads constraints via `LoraSimulationConfig`.
- Uses `LoraSimulatedLink` internally.
- Exposes `TransportClient` interface to core.
- Registers with SPI file:
  - `lora-simulation/src/main/resources/META-INF/services/core.transport.TransportPlugin`

### 5.5 Selection Flow

1. Read `transport.mode` from component config.
2. `TransportClientFactory` asks `TransportPluginRegistry` for plugin supporting mode.
3. Plugin creates a `TransportClient` bound to `ConnectionDto`.
4. `ConnectionDtoManager` calls `send()`/`retry()` on `TransportClient`.

If no plugin is found for selected mode:
- Log explicit error and fallback to `IP` (configurable strict mode optional).

---

## 6) Behavioral Changes

### 6.1 Existing (IP)

No functional change expected:

- send packet
- wait for ACK / initialization response
- retry on failure

### 6.2 New (LoRa)

For `LORA` mode, before delegate send:

- enforce MTU limit
- apply throughput/latency/jitter delay
- optionally simulate packet loss
- enforce duty-cycle pacing

ACK/retry remain controlled by the transport client contract.

---

## 7) Implementation Plan

### Phase 1 — Core Transport API

- Add `core.transport` package with interfaces and mode enum.
- Add `IpTransportPlugin` and default factory behavior.
- Add plugin registry (`ServiceLoader`).

### Phase 2 — Core Integration

- Update `ConnectionDtoManager`:
  - replace `AbstractSender sender` with `TransportClient transportClient`
  - replace `createSender()` with `createTransportClient()`
  - keep `assignedId` assignment behavior unchanged
- Keep `ConnectionManager` and keep-alive services unchanged except for using updated manager internals.

### Phase 3 — LoRa Plugin Wiring

- Add `LoraTransportPlugin` + `LoraTransportClient` adapter in `lora-simulation`.
- Register plugin via `META-INF/services`.

### Phase 4 — Configuration + Documentation

- Add transport keys to component configs.
- Update root and module READMEs with mode selection and expected behavior.

---

## 8) Detailed Change List (Planned)

## New Files

- `core/src/main/java/core/transport/TransportMode.java`
- `core/src/main/java/core/transport/TransportClient.java`
- `core/src/main/java/core/transport/TransportPlugin.java`
- `core/src/main/java/core/transport/TransportPluginRegistry.java`
- `core/src/main/java/core/transport/TransportClientFactory.java`
- `core/src/main/java/core/transport/ip/IpTransportPlugin.java`
- `core/src/main/java/core/transport/ip/IpTransportClient.java`
- `lora-simulation/src/main/java/lora/plugin/LoraTransportPlugin.java`
- `lora-simulation/src/main/java/lora/plugin/LoraTransportClient.java`
- `lora-simulation/src/main/resources/META-INF/services/core.transport.TransportPlugin`

## Modified Files

- `core/src/main/java/core/connection/ConnectionDtoManager.java`
  - replace sender creation and use plugin-backed transport creation.
- `config/node_config/nodeConfig.properties`
- `config/server_config/serverConfig.properties`
- `config/coordinator_config/coordinatorConfig.properties`
  - add `transport.mode` (default `IP`).
- `README.md`
- `lora-simulation/README.md`
  - document mode switching and plugin behavior.

---

## 9) Configuration Proposal

Add keys:

```properties
# Common transport selection
transport.mode=IP

# Optional: explicit plugin name (if needed for multiple plugins supporting same mode)
transport.plugin=

# Optional strict behavior
transport.strictMode=false
```

For LoRa deployments:

```properties
transport.mode=LORA
```

LoRa constraints remain in:

- `config/lora_simulation/loraConfig.properties`

Overrides stay supported:

- `-Dlora.config.path=...`
- `LORA_CONFIG_PATH`

---

## 10) Error Handling & Fallback Strategy

- If selected mode plugin is unavailable:
  - when `transport.strictMode=false`: fallback to IP + warning log
  - when `transport.strictMode=true`: fail startup with clear error
- If LoRa constraint config fails to load: use current default values from `LoraSimulationConfig`.

---

## 11) Compatibility & Migration Impact

- Existing deployments continue with `transport.mode=IP` (or omitted default).
- Packet format and existing handlers remain unchanged.
- No migration needed for packet handlers/listeners.
- Operational teams gain ability to switch mode per deployment config.

---

## 12) Test Plan

1. **Unit tests**
   - `TransportClientFactory` mode selection and fallback behavior
   - `TransportPluginRegistry` plugin discovery
   - `LoraTransportClient` enforcement of LoRa simulation rules
2. **Integration tests**
   - Node/Server/Coordinator startup with `transport.mode=IP`
   - Startup with `transport.mode=LORA` and plugin present
   - Startup with `transport.mode=LORA` and plugin missing (strict false/true)
3. **Regression checks**
   - ACK and initialization assignment flow unchanged
   - Keep-alive behavior unchanged in IP mode

---

## 13) Risks and Mitigations

- **Risk:** Hidden coupling to `AbstractSender` methods.
  - **Mitigation:** Keep `TransportClient` method set aligned with existing send/retry/getAssignedId behavior.
- **Risk:** Classpath plugin discovery issues.
  - **Mitigation:** Add startup diagnostics listing discovered plugins.
- **Risk:** Confusion over mode defaults.
  - **Mitigation:** Document default mode and config precedence clearly.

---

## 14) Acceptance Criteria

- A component can select `IP` or `LORA` using config only.
- LoRa mode routes sends through plugin path without direct changes to packet handling logic.
- IP mode behavior matches current system behavior.
- If LoRa plugin is absent, behavior matches configured fallback policy.

---

## 15) Summary

This design introduces a stable transport abstraction in core and integrates LoRa as a true plugin. It keeps current communication behavior intact for IP while enabling configurable transport selection and cleaner long-term extensibility.