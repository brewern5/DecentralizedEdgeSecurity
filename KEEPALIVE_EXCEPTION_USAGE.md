# KeepAliveException Usage Guide

## Overview
The `KeepAliveException` provides detailed diagnostics for keep-alive failures, helping to distinguish between different failure scenarios.

## Exception Stages

### 1. SEND_FAILED
- **Cause**: Packet failed to send due to socket/network issues
- **When**: Socket creation fails, network unreachable, etc.
- **Example**: Connection timeout, refused connection

### 2. ACK_NOT_RECEIVED
- **Cause**: Packet was sent but no ACK was received
- **When**: Remote endpoint doesn't respond, ACK packet lost in transit
- **Example**: Coordinator is down or network is dropping packets

### 3. ACK_NOT_HANDLED
- **Cause**: ACK packet was received but not processed correctly
- **When**: Handler doesn't recognize ACK, handler throws exception, ACK routing issue
- **Example**: Your current issue - ACK arrives but handler isn't processing it

### 4. ACK_ERROR_RESPONSE
- **Cause**: ACK contains an error response
- **When**: Remote endpoint actively rejects the keep-alive
- **Example**: Coordinator sends error response in ACK

### 5. TIMEOUT
- **Cause**: Operation timed out
- **When**: Keep-alive takes too long to complete

### 6. UNKNOWN
- **Cause**: General/unspecified failure

## Current Implementation

### Automatic Detection
Currently, the exception is thrown with `SEND_FAILED` stage when:
- `ConnectionDtoManager.send()` returns `false`
- This means either the packet wasn't sent OR ACK wasn't received

### For Your ACK Handling Issue
To diagnose your specific issue where ACK is sent but not handled:

1. **Add logging in your ACK handler** to verify ACK reception:
```java
// In your packet handler (likely ServerHandler or similar)
if (packet.getPacketType() == PacketType.ACK) {
    logger.info("ACK received for connection: {}", connectionId);
    // Your ACK handling logic
}
```

2. **Throw ACK_NOT_HANDLED exception** when you detect the issue:
```java
// If you can detect that ACK arrived but wasn't processed:
throw new KeepAliveException(
    KeepAliveException.FailureStage.ACK_NOT_HANDLED,
    connectionId,
    instanceId,
    "ACK received but handler failed to process it properly"
);
```

3. **Enhance PacketSender to track ACK reception separately**:
```java
// In PacketSender.send() or retry():
logger.info("Packet sent successfully to {}:{}", ip, sendingPort);
// ... wait for ACK ...
if (ackReceived) {
    logger.info("ACK received from {}:{}", ip, sendingPort);
} else {
    logger.warn("No ACK received from {}:{}", ip, sendingPort);
}
```

## Exception Properties

```java
KeepAliveException e = ...;

// Get failure stage
FailureStage stage = e.getStage();

// Get connection details
String connectionId = e.getConnectionId();
String instanceId = e.getInstanceId();
int attemptNumber = e.getAttemptNumber();

// Check failure category
boolean isSend = e.isSendFailure();      // SEND_FAILED
boolean isAck = e.isAckFailure();         // ACK_NOT_RECEIVED, ACK_NOT_HANDLED, ACK_ERROR_RESPONSE
```

## Logging Examples

The updated code now provides detailed logs:

### Success Case:
```
DEBUG - Attempting to send KEEP_ALIVE packet to connection: coordinator-1
DEBUG - Successfully sent KEEP_ALIVE packet to connection: coordinator-1 and received ACK
DEBUG - Keep-alive sent successfully to connection: coordinator-1
```

### Failure Case (No ACK):
```
DEBUG - Attempting to send KEEP_ALIVE packet to connection: coordinator-1
WARN  - Initial send failed for connection: coordinator-1, attempting retry...
WARN  - Failed to receive ACK - retrying...
ERROR - Retry failed for connection: coordinator-1 - packet was NOT sent or ACK was NOT received
ERROR - Keep-alive failed for connection: coordinator-1
ERROR - [SEND_FAILED] Failed to send keep-alive packet or receive ACK | Connection: coordinator-1 | Instance: server-1
```

### Your Current Issue (ACK Received but Not Handled):
To properly diagnose this, you need to:
1. Add logging in the ACK handler to confirm ACK arrival
2. Trace why the handler isn't processing it
3. Potentially throw `ACK_NOT_HANDLED` exception from the handler

## Next Steps for Your Issue

1. **Find your ACK handler** - Look for packet type handlers that process ACK packets
2. **Add detailed logging** to trace ACK reception and processing
3. **Verify ACK routing** - Ensure ACK packets are routed to the correct handler
4. **Check handler logic** - Verify the handler correctly processes keep-alive ACKs vs other ACKs

The exception framework is now in place to help you track exactly where failures occur!
