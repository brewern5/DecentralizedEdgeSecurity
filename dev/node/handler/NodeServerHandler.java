package handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import config.NodeConfig; 

public class NodeServerHandler implements Runnable {

    private Socket serverSocket;

    private NodeConfig config = new NodeConfig();

    private String serverIP;

    private int serverReceivingPort;

    public NodeServerHandler(Socket socket) {
        this.serverSocket = socket;
    }

    @Override
    public void run() {

        System.out.println("Server connected: " + serverSocket.getInetAddress().toString() + "\n\tFrom port: "
                + serverSocket.getLocalPort());

        serverIP = serverSocket.getInetAddress().toString();
        serverIP = serverIP.substring(1); // Removes the forward slash

        serverReceivingPort = config.getPortByKey("Server.listeningPort");

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
            BufferedReader reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            String line = reader.readLine();
            if (line != null) {
                System.out.println("Server: \n\t" + line);
                PrintWriter output = new PrintWriter(serverSocket.getOutputStream(), true);
                output.println("Hi, edge server, this is the edge node!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}