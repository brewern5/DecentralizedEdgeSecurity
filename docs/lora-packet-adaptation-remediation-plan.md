# LoRa Packet Adaptation Remediation Plan

- **Project:** DecentralizedEdgeSecurity
- **Status:** In Progress
- **Date:** 2026-02-27
- **Author:** GitHub Copilot (GPT-5.3-Codex)

---

## 1) Executive Summary

The LoRa transport integration is active, but it currently behaves as a **constraint gate** (accept/drop) rather than a **LoRa adaptation layer** (transform/send/reassemble).

Observed runtime evidence:

- `Packet ... exceeds LoRa MTU (241 bytes > 51 bytes). Dropping.`
- `Initial send failed ... attempting retry...`

This confirms packets are entering the LoRa path, but the payload format is still the original full JSON packet and is not being reshaped for LoRa limits.

---

## 2) Problem Statement

When `transport.mode=LORA`, packets should be made LoRa-compatible before transmission. Today, oversized packets are dropped and retried unchanged, causing repeated failure.

### What is happening now

1. Core sends full packet (`AbstractPacket.toDelimitedString()` JSON + `||END||`).
2. `LoraSimulatedLink` calculates envelope size.
3. If size exceeds `lora.mtuBytes`, packet is dropped.
4. Retry sends the same oversized packet again.

### Why this is a correctness issue

- Transport selection works, but transport adaptation does not.
- Reliability logic retries an impossible send condition.
- LoRa mode cannot support normal control/data packets above MTU.

---

## 3) Root Cause Analysis

### Confirmed in code

- `lora-simulation/.../LoraSimulatedLink.java`
  - Enforces MTU and drops when oversized.
  - Delegates send of original packet object.
- `lora-simulation/.../LoraTransportClient.java`
  - Wraps `LoraSimulatedLink`, no packet transformation stage.
- `core/.../AbstractSender.java`
  - Sends whole packet string in one shot over socket.

### Missing capability

No implementation for:

- fragmentation/chunking,
- reassembly,
- message framing for multi-fragment packets,
- duplicate/out-of-order handling,
- retry policy at fragment granularity.

---

## 4) Proposed Engineering Approach

Implement a **LoRa Adaptation Layer** in the `lora-simulation` module, without changing business packet types.

### 4.1 Functional design

Add a lightweight LoRa frame protocol:

- `messageId` (UUID)
- `fragmentIndex`
- `fragmentCount`
- `payloadChunk` (base64 or UTF-8-safe chunk)
- `crc` (optional but recommended)
- `isLast` (optional if `fragmentCount` present)

Outbound path:

1. Serialize packet once (`toDelimitedString`).
2. Compute max payload per LoRa frame = MTU - LoRa overhead - frame metadata overhead.
3. Split into fragments.
4. Send each fragment through `LoraSimulatedLink`.
5. Require ACK policy per fragment (or small window) and retransmit only failed fragments.

Inbound path:

1. Buffer fragments by `messageId`.
2. Reassemble when complete.
3. Validate completeness/CRC.
4. Reconstruct original packet payload and pass to normal deserialization.

### 4.2 Scope boundaries

- Keep existing application packet schema unchanged.
- Keep `TransportClient` abstraction.
- Implement adaptation only in LoRa plugin path.

---

## 5) Implementation Plan (Phased)

### Current progress

- ✅ Phase 1 complete (`LoraFrame`, `LoraFrameCodec`, `LoraFragmenter`, `LoraReassembler`, protocol unit tests)
- ✅ Phase 2 complete (fragment-first sender flow in `LoraTransportClient`, frame transmission API in `LoraSimulatedLink`, per-fragment retry tests)
- ⏳ Phase 3 pending (receiver-side reassembly and handler integration)

### Phase 1 — Protocol and model

- Add `LoraFrame` model + serializer/deserializer.
- Add `LoraFragmenter` and `LoraReassembler`.
- Add tests for boundary sizes and exact MTU math.

### Phase 2 — Sender integration

- Extend `LoraTransportClient` to fragment before send.
- Update `LoraSimulatedLink` to send frame payloads (not raw full packet only).
- Implement per-fragment retry strategy.

### Phase 3 — Receiver integration

- Add reassembly handling on LoRa receive path.
- Ensure existing handlers only see complete original packets.

### Phase 4 — Reliability hardening

- Timeouts/eviction for incomplete reassembly buffers.
- Idempotency handling for duplicate fragments.
- Metrics/logging for fragment loss and reassembly failures.

### Phase 5 — Validation

- Unit tests: fragment math, reassembly correctness, retry logic.
- Integration tests: packets above MTU (e.g., 241 bytes) successfully delivered in LoRa mode.
- Regression tests: IP mode behavior unchanged.

---

## 6) Risks and Mitigations

- **Risk:** Added complexity in ACK semantics.
  - **Mitigation:** Start with stop-and-wait fragment ACK; optimize later.
- **Risk:** Memory leak from incomplete reassembly buffers.
  - **Mitigation:** TTL-based buffer cleanup.
- **Risk:** Fragment metadata overhead reduces useful payload too much.
  - **Mitigation:** Centralize MTU budget calculator and test with worst-case headers.

---

## 7) Acceptance Criteria

1. In `transport.mode=LORA`, packets larger than configured MTU are fragmented and delivered (not dropped solely due to size).
2. Reassembly reconstructs the exact original delimited packet payload.
3. Retry occurs at fragment level; repeated whole-message failure loop is eliminated.
4. Existing IP transport behavior remains unchanged.
5. Logs show fragment/reassembly lifecycle instead of repeated MTU-drop warnings for normal oversized payloads.

