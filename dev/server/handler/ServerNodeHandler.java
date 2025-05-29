package handler;

import java.io.*;
import java.net.Socket;

import config.ServerConfig;

public class ServerNodeHandler implements Runnable {

    private Socket nodeSocket;

    private ServerConfig config = new ServerConfig();

    // Stores IP nd connection port
    private String nodeIP;

    private int nodeReceivingPort;

    public ServerNodeHandler(Socket socket) {
        this.nodeSocket = socket;
    }

    @Override
    public void run() {

        System.out.println(
                "Node connected: " + nodeSocket.getInetAddress().toString() + "\n\tFrom port: " + nodeSocket.getPort());

        // Store IP and port locally
        nodeIP = nodeSocket.getInetAddress().toString();
        nodeIP = nodeIP.substring(1); // Removes the forward slash

        // Grab sending port and store locally
        nodeReceivingPort = config.getPortByKey("Node.sendingPort");

        // Handle client events
        try {
            // Thread for client
            // Send ack back on that same connection line
            // THREADS!
            // Define packet structure
            // Create seperate class for the packet
            // Instantiate a new packet class and will populate that instance
            // Create a packet DTO
            // DTO (Look into this)
            BufferedReader reader = new BufferedReader(new InputStreamReader(nodeSocket.getInputStream()));

            String line = reader.readLine();
            if (line != null) {
                System.out.println("Node: \n\t" + line);
                PrintWriter output = new PrintWriter(nodeSocket.getOutputStream(), true);
                output.println("Hi, edge node, this is the edge server!");
            }

            // TODO: Do stuff here with message

            // Acknowledge

            reader.close();

            // TODO: check some other things before closing the sockets
            nodeSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}