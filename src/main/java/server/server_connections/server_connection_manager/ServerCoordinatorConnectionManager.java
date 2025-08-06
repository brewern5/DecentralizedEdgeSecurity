/*
 *      Author: Nathaniel Brewer
 * 
 *      Since The server is handling information from both the Coordinator and the Nodes
 *      It needs to have two differnet storages for connections. This is the simpler way 
 *      than refactoring the Superclass of the handler
 */

package server.server_connections.server_connection_manager;

public class ServerCoordinatorConnectionManager extends ServerConnectionManager {

    // Step 1: (For my sanity) create an instance variable
    private static ServerNodeConnectionManager instance;

    // Step 3: Get instance (if one is not there it is created then returned)
    public static synchronized ServerConnectionManager getInstance() {
        if (instance == null) {
            instance = new ServerNodeConnectionManager();
        }
        return instance;
    }
}
