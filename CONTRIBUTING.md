# Contributing Guide

## Scope

This repository currently uses the refactored component architecture under `src/main/java`.
Legacy code under `src/legacy` is not the primary extension target unless a task explicitly requires it.

## Current Topology Assumption

The runtime currently behaves as a 3-tier topology:

- Coordinator
- Server
- Node

Contributions should preserve existing runtime behavior unless the change explicitly targets topology evolution.

## Adding a New Packet Type

Use this workflow to keep packet handling extendable and consistent:

1. Add enum value in `core.packet.PacketType`.
2. Add packet class extending `core.packet.AbstractPacket`.
3. Add packet manager extending `core.packet.AbstractPacketManager` if request processing is required.
4. Add `core.packet.PacketManagerProvider` implementation for manager resolution.
5. Add `core.external.PacketSubtypeProvider` implementation for Gson subtype mapping.
6. Register providers in:
	- `src/main/resources/META-INF/services/core.packet.PacketManagerProvider`
	- `src/main/resources/META-INF/services/core.external.PacketSubtypeProvider`
7. Build and verify behavior:
	- `mvn -DskipTests compile`

## Handler Extension Rules

- Keep transport flow in `core.handler.AbstractSocketPacketHandler`.
- Keep business packet logic in packet managers.
- Avoid introducing packet-type switch logic back into handlers.

## Javadoc Standards for Extendable Areas

For extension points, include:

- Contract expectations (inputs, outputs, invariants)
- Failure behavior and exception conditions
- How to add a new implementation

Required targets for meaningful changes:

- `AbstractPacket`
- `AbstractPacketManager`
- `PacketManagerProvider`
- `PacketSubtypeProvider`
- `PacketManagerFactory`
- Shared handler abstractions

## Validation Checklist Before Merge

1. Project compiles with Maven.
2. New packet type can be deserialized through `PacketTypeAdapterFactory`.
3. New packet type can be dispatched through `PacketManagerFactory`.
4. No handler-level packet-type branching was added unless absolutely necessary.
