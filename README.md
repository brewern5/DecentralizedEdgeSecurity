# DecentralizedEdgeSecurity

## Overview

DecentralizedEdgeSecurity is a research project for building a basic 3-tiered edge network implemented in Java with Maven. The system consists of a Coordinator, Server, and Node, each running as separate Java processes that establish TCP connections and exchange JSON-formatted messages to demonstrate connectivity between network tiers.

**Current features:**
- **Maven-based build system** with proper dependency management
- **Hierarchical package structure** following Java naming conventions
- **JSON packet communication** using Gson library for serialization
- **Multi-threaded architecture** with concurrent connection handling
- **Extensible packet system** supporting multiple message types
- **TCP socket management** with proper connection lifecycle handling
- **Comprehensive logging** using Log4j2 framework

---

## Architecture

### Three-Tier Network Structure (Current Ports)
```
┌─────────────────────────┐
│      Coordinator        │
│  listens: 4001          │
└─────────────────────────┘
            ▲
            │ init + commands
            │
            ▼
┌─────────────────────────┐
│         Server          │
│  listens:               │
│    from coordinator 5003│
│    from nodes       5004│
└─────────────────────────┘
            ▲
            │ init + data
            │
            ▼
┌─────────────────────────┐
│          Node           │
│  listens: 6001          │
└─────────────────────────┘
```

### Control and Data Flow
- Startup order: Coordinator → Server → Node (matching run scripts).
- Server → Coordinator: Sends initialization with its coordinator-facing port; Coordinator assigns ID and tracks connection via `CoordinatorConnectionManager`.
- Node → Server: Sends initialization with its listening port; Server assigns Node/Cluster IDs and manages via `ServerNodeConnectionManager`.
- Keep-alives: Coordinator and Server timers check for expired connections; Node timers handle keep-alives to Server.
- Message routing: Servers bridge messages between Nodes and Coordinator; payloads remain JSON packets with `packetType/sender/payload`.

---

## Project Structure

```
DecentralizedEdgeSecurity/
├── pom.xml
├── config/
│   ├── ip/
│   │   ├── coordinator_config/
│   │   ├── server_config/
│   │   └── node_config/
│   └── lora/
│       ├── coordinator_config/
│       ├── server_config/
│       ├── node_config/
│       └── lora_simulation/
├── core/
├── lora-simulation/
└── scripts/
   ├── ip/
   │   ├── windows/
   │   └── linux/
   └── lora/
      ├── windows/
      └── linux/
```

---

## Dependencies

The project uses Maven for dependency management with the following key libraries:

- **Java 17** - Target runtime environment
- **Gson 2.13.1** - JSON serialization/deserialization
- **Log4j 2.23.1** - Logging framework
- **Error Prone Annotations** - Code quality annotations

Dependencies are automatically managed by Maven and stored in `target/classes` after compilation.

---

## Coordinator

The Coordinator acts as the entry point for Servers in the network. When a Server connects, the Coordinator performs the following steps:

1. **Connection Handling:**  
   The Coordinator listens for incoming TCP connections from Servers on a configured port. Each new connection is handled in a separate thread to allow concurrent processing.

2. **Packet Reception and Parsing:**  
   Upon receiving a connection, the Coordinator reads a line of input from the Server. This input is expected to be a JSON-formatted string representing a packet. The Coordinator uses the Gson library to parse this JSON into a `CoordinatorPacket` object, which contains:
   - `packetType`: An enum indicating the type of message (e.g., INITIALIZATION, AUTH, MESSAGE, etc.).
   - `sender`: The identity of the sender.
   - `payload`: The message content.

3. **Packet Type Handling:**  
   The Coordinator inspects the `packetType` field and uses a switch statement to determine how to process the message:
   - **INITIALIZATION:** Handles handshake/setup logic for new Servers. This may include storing the Server's IP and preparing for further communication.
   - **AUTH:** Placeholder for future authentication logic.
   - **MESSAGE, COMMAND, HEARTBEAT, STATUS, DATA, ERROR, ACK, DISCONNECT:** Each type is recognized, and the Coordinator can be extended to process these accordingly. Currently, only the INITIALIZATION type is handled with any logic; others are placeholders for future development.

4. **Response:**  
   After processing the packet, the Coordinator sends a simple greeting response ("Hi, edge server, this is the edge coordinator!") back to the Server over the same connection.

5. **Connection Closure:**  
   The Coordinator closes the input/output streams and the socket after handling the message.

This design allows the Coordinator to flexibly handle different types of messages from Servers and provides a foundation for implementing more advanced logic (such as authentication or status tracking) in the future.

---

## Server

The Server is a crucial component that connects to the Coordinator and manages communication with Nodes. Its responsibilities include:

- **Initialization and Registration:**
  - Connects to the Coordinator and sends an INITIALIZATION packet containing its configuration (e.g., listening port).
  - Waits for an ACK from the Coordinator to confirm successful registration.

- **Node Communication:**
  - Listens for incoming connections from Nodes on its configured port.
  - Receives and parses packets from Nodes using the generic packet structure.
  - Responds to Node messages and can send/receive various packet types (e.g., MESSAGE, ACK).

- **Packet Parsing and Handling:**
  - Inspects the `packetType` field of received packets to determine the type of message.
  - Handles MESSAGE and ACK packet types with appropriate logic (e.g., printing messages, sending acknowledgments).

- **Thread Management:**
  - Manages each Node connection in a separate thread for concurrent processing.
  - Demonstrates proper socket, stream, and thread management for robust operation.

---

## Node

- Connects to the Server and sends a greeting.
- Prints the response from the Server.

---

## Packet Structure: Generic Packet

All packets exchanged between components (Coordinator, Server, Node) follow a common JSON structure. This structure allows for flexible message types and payloads.

**Fields:**
- `packetType`: The type of the packet (e.g., INITIALIZATION, AUTH, MESSAGE, COMMAND, HEARTBEAT, STATUS, DATA, ERROR, ACK, DISCONNECT).
- `sender`: The name or identifier of the sender (e.g., EdgeServer, EdgeNode, Coordinator).
- `payload`: The message content, which may be a string, JSON object, or key-value pairs depending on the packet type.

**Example JSON:**
```json
{
  "packetType": "MESSAGE",
  "sender": "EdgeNode",
  "payload": "Hello, server!"
}
```

**Notes:**
- The meaning and format of `payload` depend on the `packetType`.
- All packets must include these three fields.

---

## Packet Structure: INITIALIZATION Type

When a Server connects to the Coordinator, it sends an INITIALIZATION packet. This packet is serialized as JSON and contains the following fields:

- `packetType`: The type of the packet. For initialization, this is `INITIALIZATION`.
- `sender`: The name or identifier of the sender (e.g., `EdgeServer`).
- `payload`: A string containing key-value pairs separated by semicolons, describing initialization parameters (e.g., the server's listening port).

**Example JSON:**
```json
{
  "packetType": "INITIALIZATION",
  "sender": "EdgeServer",
  "payload": "server.listeningPort:5003"
}
```

**Payload Format:**
- The payload is a string of key-value pairs separated by semicolons (`;`).
- Each key and value are separated by a colon (`:`).
- Example: `"server.listeningPort:5003;anotherKey:anotherValue"`

**Coordinator Handling:**
- The Coordinator parses the payload, extracts each key-value pair, and stores them in its configuration.
- After processing, the Coordinator responds with an ACK packet to confirm successful initialization.

---

## Getting Started

### Prerequisites
- **Java 17 or higher** installed and configured
- **Maven 3.6+** for build management
- **Git** for version control

### Quick Start

1. **Clone the repository:**
   ```bash
   git clone https://github.com/brewern5/DecentralizedEdgeSecurity.git
   cd DecentralizedEdgeSecurity
   ```

2. **Build and run the entire system:**
   ```bash
   # Windows (IP profile)
   scripts\ip\windows\run-all.bat

   # Linux (IP profile)
   scripts/ip/linux/run-all.sh

   # Windows (LoRa profile)
   scripts\lora\windows\run-all.bat

   # Linux (LoRa profile)
   scripts/lora/linux/run-all.sh
   
   # Or manually with Maven
   mvn -DskipTests clean package
   ```

3. **The scripts will:**
   - Clean and compile all Java sources using Maven
   - Download and copy runtime dependencies into module `target/dependency` folders
   - Launch three separate terminal windows for Coordinator, Server, and Node
   - Start each component with the selected transport profile (`ip` or `lora`)

### Manual Execution

If you prefer to run components individually:

```bash
# Compile the project
mvn -DskipTests clean package

# Copy runtime dependencies
mvn -pl core dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=core/target/dependency
mvn -pl lora-simulation dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=lora-simulation/target/dependency

# Run Coordinator
java -Ddes.transport.profile=ip -Dtransport.mode=IP -cp core/target/classes;core/target/dependency/* components.coordinator.EdgeCoordinator

# Run Server (in new terminal)
java -Ddes.transport.profile=ip -Dtransport.mode=IP -cp core/target/classes;core/target/dependency/* components.server.EdgeServer

# Run Node (in new terminal)
java -Ddes.transport.profile=ip -Dtransport.mode=IP -cp core/target/classes;core/target/dependency/* components.node.EdgeNode
```

### Configuration

Configuration files are profile-based in the `config/` directory:

- `config/ip/coordinator_config/coordinatorConfig.properties`
- `config/ip/server_config/serverConfig.properties`
- `config/ip/node_config/nodeConfig.properties`
- `config/lora/coordinator_config/coordinatorConfig.properties`
- `config/lora/server_config/serverConfig.properties`
- `config/lora/node_config/nodeConfig.properties`
- `config/lora/lora_simulation/loraConfig.properties`

Profile selection is controlled by:

- `-Ddes.transport.profile=ip|lora` (component config profile, default `ip`)
- `-Dtransport.mode=IP|LORA` (transport implementation mode)
- `-Dlora.config.path=...` (optional override for LoRa constraints file)

Resolution behavior:

- `des.transport.profile` takes precedence when set.
- If `des.transport.profile` is unset, `transport.mode=LORA` automatically selects the `lora` component profile.
- If neither is set, component profile defaults to `ip`.
- Values are normalized (trimmed and case-insensitive), so values like `"  LoRa  "` are treated as `lora`.

Each component reads its respective configuration on startup.

### Script Matrix

- **IP / Windows**
   - `scripts\ip\windows\run-all.bat`
   - `scripts\ip\windows\run-all-2nodes.bat`
   - `scripts\ip\windows\run-coord-server.bat`
   - `scripts\ip\windows\run-coord.bat`
- **IP / Linux**
   - `scripts/ip/linux/run-all.sh`
- **LoRa / Windows**
   - `scripts\lora\windows\run-all.bat`
   - `scripts\lora\windows\run-all-2nodes.bat`
   - `scripts\lora\windows\run-coord-server.bat`
   - `scripts\lora\windows\run-coord.bat`
- **LoRa / Linux**
   - `scripts/lora/linux/run-all.sh`

---

## Development

### Building from Source
```bash
# Clean build
mvn clean compile

# Run tests (when implemented)
mvn test

# Package application
mvn package
```

### IDE Setup
The project follows standard Maven conventions and can be imported into any Java IDE:
- **IntelliJ IDEA**: Open the `pom.xml` file
- **Eclipse**: Import as Maven project
- **VS Code**: Open folder with Java Extension Pack

### Package Structure
All packages follow hierarchical naming:
- `coordinator.*` - Coordinator module packages
- `server.*` - Server module packages  
- `node.*` - Node module packages

---

## Current Status

> **Note:** This project is in active development and demonstrates basic connectivity and message exchange between the three network tiers. Future enhancements will include authentication, encryption, load balancing, and advanced edge computing capabilities.
