/*
 *      Author: Nathaniel Brewer
 * 
 *      This is where all the ConnectionInfo objects will be stored and where the storage wise checks will be had for
 *      expired keepAlive.
 * 
 *      This is desined as a Singleton Pattern, to prevent multiple instances of this connection map.
 *      
 */
package server.server_connections.server_connection_manager;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_connections.ServerConnectionInfo;
import server.server_connections.ServerPriority;

public abstract class ServerConnectionManager {

    protected final Map<String, ServerConnectionInfo> activeConnections = new ConcurrentHashMap<>();

    // Each class can have its own logger instance
    private static final Logger logger = LogManager.getLogger(ServerConnectionManager.class);

    public void checkExpiredConnections() {
        // Create an iterator for the ConccurentHashMap since it needs an iterator
        Iterator<Map.Entry<String , ServerConnectionInfo>> iterator =
            activeConnections.entrySet().iterator();

        
        while(iterator.hasNext()) {
            // Check if each entry is expired, if it is, check the priority to see if it needs to be removed or kept alive
            Map.Entry<String, ServerConnectionInfo> entry = iterator.next();
            if(entry.getValue().isExpired()){
                if(entry.getValue().getServerPriortiy() == ServerPriority.CRITICAL){
                    // TODO: KEEP ALIVE
                }
                else {
                    terminateConnection(entry.getValue().getId());
                }
            }
        }
    }

    /*
     * 
     *      Response to expiry
     * 
     */

    public void keepAlive(){

    }

    public void terminateConnection(String id) {
        ServerConnectionInfo remove = activeConnections.get(id);
        logger.info("Terminated Connection: \n  ID:" + remove.getId() 
            + "\n  IP - " + remove.getIp() + ":" + remove.getPort()
        );
        activeConnections.remove(remove.getId());
    }

    /*
     * 
     *      Mutators
     * 
     */

    // Can allow for multiple connections to be added at once(if needed)
    public void addConnection(ServerConnectionInfo... connection) {
        for(ServerConnectionInfo connected : connection) {
            activeConnections.put(connected.getId(), connected);
        }
    }

    // When recieving a 
    public void updateById(String id, Object updator) {
        ServerConnectionInfo updatee = activeConnections.get(id);

        // TODO: Figure out a system for updating

       // updatee.update(updator)
    }

    /*
     * 
     *      Getters
     * 
     */
    public ServerConnectionInfo getConnectionInfoById(String id) {
        return activeConnections.get(id);
    }

    public Map<String, ServerConnectionInfo> getActiveConnections() {
        return activeConnections;
    }

    public String[] getAllIds() {
        return activeConnections.keySet().toArray(new String[0]);
    }
}
