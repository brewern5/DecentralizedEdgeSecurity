# LoRa Configuration Reference

- **Project:** DecentralizedEdgeSecurity
- **Module:** lora-simulation
- **Config file:** `config/lora/lora_simulation/loraConfig.properties`

This guide explains each LoRa simulation property, what it controls, and why it matters when reading or tuning deployments.

## How config is loaded

`lora.config.path` (JVM system property) and `LORA_CONFIG_PATH` (environment variable) override the default config location.

Resolution order:
1. `-Dlora.config.path=...`
2. `LORA_CONFIG_PATH`
3. `config/lora/lora_simulation/loraConfig.properties`

If a property is missing or invalid, `LoraSimulationConfig` falls back to built-in defaults.

## Property reference

| Property | Default | What it controls | Why it matters |
|---|---:|---|---|
| `lora.mtuBytes` | `51` | Maximum allowed frame size on simulated LoRa link. | Primary limiter for payload delivery. Frames above MTU are rejected, so this determines how aggressively fragmentation is required. |
| `lora.perPacketOverheadBytes` | `13` | Estimated non-payload bytes added per transmission (headers/framing). | Effective payload is `mtu - overhead`; increasing overhead reduces useful data per frame and increases fragment count. |
| `lora.uplinkBitsPerSecond` | `5000` | Simulated uplink throughput (sender to receiver). | Controls airtime delay before send completes. Lower values increase latency and can expose timeout/retry behavior. |
| `lora.downlinkBitsPerSecond` | `5000` | Simulated downlink throughput (receiver to sender). | Used for link modeling symmetry and future ACK/downlink timing realism. Keep consistent with expected LoRa profile. |
| `lora.latencyMs` | `250` | Base one-way latency added to each transmission. | Models propagation/processing delay; impacts end-to-end response times and retry thresholds. |
| `lora.jitterMs` | `50` | Random extra delay added on top of base latency. | Introduces timing variability; useful for testing robustness against non-deterministic network conditions. |
| `lora.packetLossProbability` | `0.02` | Probability (0.0–1.0) of dropping a packet in simulation. | Exercises retry and resilience logic. Higher values stress reliability paths and can reveal retransmission bugs. |
| `lora.dutyCycleWindowMs` | `60000` | Time window used for duty-cycle pacing. | Defines the rate-limiting interval. Combined with max transmissions, it models regulatory/channel usage constraints. |
| `lora.maxTransmissionsPerWindow` | `30` | Maximum transmissions allowed per duty-cycle window. | Caps transmit burst volume. Lower values enforce more waiting, increasing queueing and total message latency. |

## Practical tuning profiles

### Fast local functional testing
- Keep packet loss low (`0.0` to `0.01`)
- Keep latency/jitter modest
- Keep MTU realistic if validating fragmentation logic

### Stress reliability behavior
- Increase `packetLossProbability` (e.g., `0.05` to `0.20`)
- Increase jitter and/or lower throughput
- Keep duty-cycle limits strict to trigger pacing

### Throughput-constrained realism
- Reduce `uplinkBitsPerSecond`
- Keep MTU small (LoRa-like)
- Tune `maxTransmissionsPerWindow` conservatively

## Safety notes

- Extremely small `lora.mtuBytes` combined with high overhead can make frames impossible to send.
- Very high loss plus strict duty-cycle can make progress slow or appear stalled.
- Keep values aligned with test intent; do not mix “fast dev” and “realistic constrained link” settings in the same profile.

## Source of truth in code

- `lora.config.LoraSimulationConfig`
- `lora.model.LoraConstraints`
- `lora.simulation.LoraSimulatedLink`
