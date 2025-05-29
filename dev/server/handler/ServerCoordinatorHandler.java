package handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import config.ServerConfig;

public class ServerCoordinatorHandler implements Runnable {

    private Socket coordinatorSocket;

    private ServerConfig config = new ServerConfig();

    // As soon as the connection is made, it will store the IP and the Port used to send the message
    private String coordinatorIP;

    private int coordinatorRecievingPort;

    public ServerCoordinatorHandler(Socket socket) {
        this.coordinatorSocket = socket;
    }

    @Override
    public void run() {

        System.out.println("Coordinator connected: " + coordinatorSocket.getInetAddress().toString() + "\n\tFrom port: "
                + coordinatorSocket.getPort());

        // Store IP and port locally
        coordinatorIP = coordinatorSocket.getInetAddress().toString();
        coordinatorIP = coordinatorIP.substring(1); // Removes the forward slash

        // grab sending port from the config file
        coordinatorRecievingPort = config.getPortByKey("Coordinator.sendingPort");

        // Handle client events
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(coordinatorSocket.getInputStream()));

            String line = reader.readLine();
            if (line != null) {
                System.out.println("Coordinator: \n\t" + line);
                PrintWriter output = new PrintWriter(coordinatorSocket.getOutputStream(), true);
                output.println("Hi, edge coordinator, this is the edge server!");
            }

            // TODO: Do stuff here with message

            // Acknowledge

            reader.close();

            // TODO: check some other things before closing the sockets
            coordinatorSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}