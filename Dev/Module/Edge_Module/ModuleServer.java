package Edge_Module;

import java.net.*;

import config.Config;       // The configuration file for the entire network. Located in ../config/config.java

public class ModuleServer {

    private static String IP;
    private static int port;

    private static ServerSocket listening;
    private static Socket sending;
    
    public static void ModuleServer(String IP, int port) {

        Config config = new Config();

        // try/catch to generate the IP from ./Config.java - Throws UnknownHostException if it cannot determine the IP
        try{
            IP = config.grabIP();
        } catch (UnknownHostException e) {
            System.err.println("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        }

        port = config.generatePort("edgeModule.port");

        // Try to create a serverSocket to listen to requests 
        try {
            listening = new ServerSocket(port);
            System.out.println("Listening on port " + port);
        } catch (Exception e) {
            System.err.println("Error creating Listening Socket on port " + port);
            e.printStackTrace();
            // TODO: try to grab new port
        }

    }


    /**
     * 
     *      Accessing methods
     * 
     */

    public static String getIP() {
        return IP; 
    }

    public static int getPort() {
        return port;
    }

}
