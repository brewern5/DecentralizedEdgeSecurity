# Transport Example: LoRa Simulation Model

This repository includes a LoRa simulated transport implementation as a reference example of how constrained-network transports can be integrated into the framework.

The LoRa simulation model is not intended to represent a full hardware deployment. Instead, it is provided to demonstrate how the framework behaves under constrained communication conditions.

The simulation models common LoRa-like restrictions including:

- Limited payload size (MTU constraints)
- Reduced bandwidth throughput
- Artificial latency injection
- Optional packet loss simulation
- Duty cycle-like transmission pacing

Configuration for the LoRa simulation transport is handled via external configuration files.

This module serves strictly as an example transport implementation and can be replaced or extended by users implementing their own networking models.

## Configuration reference

For a complete explanation of each LoRa property, default value, and why it matters, see:

- [LoRa Configuration Reference](../docs/lora-configuration-reference.md)

For diagrams and a step-by-step walkthrough of how packets are fragmented, frame-encoded, transmitted, and reassembled, see:

- [LoRa Data Flow](../docs/lora-data-flow.md)

## Plugin scaffolding

- `lora.model.LoraConstraints` encapsulates MTU, throughput, latency, packet loss, and duty-cycle limits.
- `lora.config.LoraSimulationConfig` loads constraints from a properties file (default: `config/lora/lora_simulation/loraConfig.properties`). Override the path with `-Dlora.config.path=/path/to/file.properties` or the `LORA_CONFIG_PATH` environment variable.
- `lora.simulation.LoraSimulatedLink` applies the constraints (size checks, transmission pacing, latency injection, and loss simulation) before delegating to the core transport.

## Quick start

1. Adjust `config/lora/lora_simulation/loraConfig.properties` to match the desired link profile (MTU, latency, bandwidth, loss, duty-cycle window).
2. Construct a `LoraSimulatedLink` with either an explicit `PacketSender` or a target IP/port and pass packets through it instead of sending directly. The link enforces the configured limits and drops or delays traffic accordingly.

## Running with LoRa profile

- **Windows:** `scripts\\lora\\windows\\run-all.bat`
- **Linux:** `scripts/lora/linux/run-all.sh`

These scripts set:

- `-Ddes.transport.profile=lora`
- `-Dtransport.mode=LORA`
- `-Dlora.config.path=config/lora/lora_simulation/loraConfig.properties`
