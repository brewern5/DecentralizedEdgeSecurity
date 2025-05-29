package handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import config.CoordinatorConfig;       // The configuration file for the entire network

public class CoordinatorServerHandler implements Runnable {
    
    private Socket serverSocket;

    private CoordinatorConfig config = new CoordinatorConfig();

    private String serverIP;

    private int serverReceivingPort;

    public CoordinatorServerHandler(Socket socket) {
        this.serverSocket = socket;
    }

    @Override
    public void run(){

        System.out.println("Server connected: " + serverSocket.getInetAddress().toString() + "\n\tFrom port: " + serverSocket.getLocalPort());

        serverIP = serverSocket.getInetAddress().toString();
        serverIP = serverIP.substring(1); // Removes the forward slash

        serverReceivingPort = config.getPortByKey("Server.recievingPort");

        // Handle client events
        try{
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
                System.out.println("Node: \n\t" + line);
                PrintWriter output = new PrintWriter(serverSocket.getOutputStream(), true);
                output.println("Hi, edge server, this is the edge coordinator!");
            }

            // TODO: Do stuff here with message

            // Acknowledge

            reader.close();

            // TODO: check some other things before closing the sockets
            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }      
    }
}
