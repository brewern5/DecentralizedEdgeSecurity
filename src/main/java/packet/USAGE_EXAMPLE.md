# PacketManagerFactory Usage Examples

## Overview
The `PacketManagerFactory` automatically creates the correct `AbstractPacketManager` subclass based on the deserialized packet's `PacketType`. This eliminates the need for manual switch statements and ensures type safety.

## Basic Usage Flow

### 1. Deserialize Incoming JSON Packet
```java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lib.PacketTypeAdapterFactory;
import packet.AbstractPacket;
import packet.AbstractPacketManager;
import packet.PacketManagerFactory;

// Setup Gson with PacketTypeAdapterFactory
Gson gson = new GsonBuilder()
    .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
    .create();

// Deserialize incoming JSON - automatically creates correct packet subtype
String incomingJson = "{\"packetType\":\"KEEP_ALIVE\",\"senderId\":\"node-123\",...}";
AbstractPacket receivedPacket = gson.fromJson(incomingJson, AbstractPacket.class);
```

### 2. Create Manager from Packet
```java
// For most packet types (KEEP_ALIVE, PEER_LIST_REQ, etc.)
AbstractPacketManager manager = PacketManagerFactory.createManager(
    receivedPacket,
    myServerId,      // Your instance ID
    clusterId,       // Cluster ID
    "Server"         // Role: "Node", "Server", or "Coordinator"
);

// Manager is already configured with the incoming packet!
AbstractPacket responsePacket = manager.processIncomingPacket();
```

### 3. Handle INITIALIZATION Packets (requires extra params)
```java
import connection.ConnectionManager;

// For INITIALIZATION packets only
if (receivedPacket.getPacketType() == PacketType.INITIALIZATION) {
    AbstractPacketManager manager = PacketManagerFactory.createManager(
        receivedPacket,
        myServerId,
        clusterId,
        "Server",
        connectionManager,    // Required for INITIALIZATION
        senderIpAddress       // IP from socket (not from packet!)
    );
    
    AbstractPacket responsePacket = manager.processIncomingPacket();
}
```

## Integration with Existing Handler Pattern

### Before (Manual Approach)
```java
// Old way - manual switch and instantiation
private Void handleKeepAlive(ServerPacket nodePacket) {
    packetHandler = new ServerKeepAliveHandler(nodeConnectionManager);
    return null;
}

private Void handlePeerListReq(ServerPacket nodePacket) {
    packetHandler = new ServerPeerReqHandler(nodePacket);
    return null;
}
```

### After (Using Factory)
```java
// In your handler class (e.g., ServerNodeHandler)
import packet.PacketManagerFactory;
import packet.AbstractPacket;
import packet.AbstractPacketManager;

private void handleIncomingPacket(String jsonPacket, String senderIp) {
    // 1. Deserialize using PacketTypeAdapterFactory
    Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
        .create();
    
    AbstractPacket receivedPacket = gson.fromJson(jsonPacket, AbstractPacket.class);
    
    // 2. Check if packet type needs a manager
    if (!PacketManagerFactory.requiresManager(receivedPacket.getPacketType())) {
        logger.warn("Received response packet type: " + receivedPacket.getPacketType());
        return; // Response packets don't need managers
    }
    
    // 3. Create appropriate manager based on packet type
    AbstractPacketManager manager;
    
    if (receivedPacket.getPacketType() == PacketType.INITIALIZATION) {
        manager = PacketManagerFactory.createManager(
            receivedPacket,
            myServerId,
            clusterId,
            "Server",
            connectionManager,
            senderIp  // From socket
        );
    } else {
        manager = PacketManagerFactory.createManager(
            receivedPacket,
            myServerId,
            clusterId,
            "Server"
        );
    }
    
    // 4. Process packet and get response (incoming packet already set!)
    AbstractPacket responsePacket = manager.processIncomingPacket();
    
    // 5. Send response back
    sendResponse(responsePacket);
}
```

## Simplified Handler Example
```java
public class SimplePacketHandler {
    private final Gson gson;
    private final String myId;
    private final String clusterId;
    private final String role;
    private final ConnectionManager connectionManager;
    
    public SimplePacketHandler(String myId, String clusterId, String role, ConnectionManager connMgr) {
        this.myId = myId;
        this.clusterId = clusterId;
        this.role = role;
        this.connectionManager = connMgr;
        
        // Setup Gson once
        this.gson = new GsonBuilder()
            .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
            .create();
    }
    
    public AbstractPacket handlePacket(String jsonPacket, String senderIp) {
        // Deserialize
        AbstractPacket receivedPacket = gson.fromJson(jsonPacket, AbstractPacket.class);
        
        // Skip response packets
        if (!PacketManagerFactory.requiresManager(receivedPacket.getPacketType())) {
            return null;
        }
        
        // Create manager and process in one flow
        AbstractPacketManager manager = receivedPacket.getPacketType() == PacketType.INITIALIZATION
            ? PacketManagerFactory.createManager(receivedPacket, myId, clusterId, role, connectionManager, senderIp)
            : PacketManagerFactory.createManager(receivedPacket, myId, clusterId, role);
        
        // Process and return response
        return manager.processIncomingPacket();
    }
}
```

## Error Handling
```java
try {
    AbstractPacket receivedPacket = gson.fromJson(jsonPacket, AbstractPacket.class);
    
    AbstractPacketManager manager = PacketManagerFactory.createManager(
        receivedPacket, myId, clusterId, role
    );
    
    AbstractPacket response = manager.processIncomingPacket();
    sendResponse(response);
    
} catch (IllegalArgumentException e) {
    logger.error("Invalid packet type or missing parameters: " + e.getMessage());
    // Send error response
    
} catch (UnsupportedOperationException e) {
    logger.error("Packet type not yet implemented: " + e.getMessage());
    // Send error response
    
} catch (Exception e) {
    logger.error("Error processing packet: " + e.getMessage());
    // Send error response
}
```

## Benefits

1. **Automatic Type Detection**: The PacketTypeAdapterFactory deserializes JSON into the correct packet subtype
2. **Single Responsibility**: Factory handles manager creation logic
3. **Type Safety**: No manual casting or type checking
4. **Reduced Boilerplate**: Eliminates repetitive switch statements
5. **Easy Extension**: Add new packet types by updating factory and PacketTypeAdapterFactory
6. **Pre-configured**: Manager already has the incoming packet set via `recreateIncomingPacket()`

## Adding New Packet Types

### 1. Create the Packet Class
```java
package packet.custom;

import packet.AbstractPacket;
import packet.PacketType;

public class CustomPacket extends AbstractPacket {
    public CustomPacket(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.CUSTOM, clusterId, recipientId);
    }
}
```

### 2. Update PacketType Enum
```java
public enum PacketType {
    // ... existing types
    CUSTOM  // Add new type
}
```

### 3. Update PacketTypeAdapterFactory
```java
public static TypeAdapterFactory create() {
    return RuntimeTypeAdapterFactory.of(AbstractPacket.class, "packetType")
        // ... existing registrations
        .registerSubtype(CustomPacket.class, "CUSTOM");
}
```

### 4. Create Manager
```java
public class CustomPacketManager extends AbstractPacketManager {
    public CustomPacketManager(String senderId, String clusterId, String recipientId, String role) {
        super(senderId, clusterId, recipientId, role);
    }
    // ... implement abstract methods
}
```

### 5. Update PacketManagerFactory
```java
case CUSTOM:
    manager = new CustomPacketManager(
        responderId,
        clusterId,
        senderId,
        role
    );
    break;
```

Done! The factory will now automatically handle your new packet type.
