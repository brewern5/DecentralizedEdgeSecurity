# DecentralizedEdgeSecurity

## Overview

DecentralizedEdgeSecurity is a research project for building a basic 3-tiered edge network. The current implementation sets up a Coordinator, a Server, and a Node, each running as a separate Java process. These components establish TCP connections and exchange simple messages to demonstrate connectivity between tiers.

**Current features:**
- Coordinator listens for connections from Servers and processes packets using a unified JSON structure.
- Server connects to the Coordinator, sends an INITIALIZATION packet, and listens for connections from Nodes.
- Node connects to the Server and exchanges messages using the generic packet format.
- All components use a flexible packet structure with `packetType`, `sender`, and `payload` fields.
- Coordinator parses and handles different packet types (e.g., INITIALIZATION, MESSAGE, ACK, etc.), with extensible logic for future types.
- Basic handshake and acknowledgment logic implemented between tiers.
- Each component demonstrates proper TCP socket management and thread handling.

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

- Connects to the Coordinator and sends a greeting.
- Listens for incoming connections from Nodes.
- Prints received messages from Nodes and responds with a greeting.

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

1. **Clone the repository:**
   ```sh
   git clone https://github.com/yourusername/DecentralizedEdgeSecurity.git
   cd DecentralizedEdgeSecurity
   ```

2. **Ensure Java 17+ is installed.**

3. **Build and run all components:**
   - Use the provided batch script:
     ```sh
     run_all.bat
     ```
   - This will compile and launch the Coordinator, Server, and Node in separate terminals.

> **Note:** This project is in an early stage and currently demonstrates only basic connectivity and message exchange between the three tiers.
