/*
 *      Author: Nathaniel Brewer
 * 
 *      This is where all the ConnectionInfo objects will be stored and where the storage wise checks will be had for
 *      expired keepAlive.
 * 
 *      This is desined as a Singleton Pattern, to prevent multiple instances of this connection map.
 *      
 */
package node.node_connections;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import node.node_packet.NodePacket;

public class NodeConnectionManager {

    // Each class can have its own logger instance
    private static final Logger logger = LogManager.getLogger(NodeConnectionManager.class);
        
    // This "ConcurentHashMap" allows for multi-threaded visability.
    private final Map<String, NodeConnectionInfo> activeConnections = new ConcurrentHashMap<>();

    // Step 1: (For my sanity) create an instance variable
    private static NodeConnectionManager instance;

    // Step 2: No-Args private constructor to prevent external instantiation
    private NodeConnectionManager() { }

    // Step 3: Get instance (if one is not there it is created then returned)
    public static synchronized NodeConnectionManager getInstance() {
        if (instance == null) {
            instance = new NodeConnectionManager();
        }
        return instance;
    }


    /*
     *      Response to expiry
     */

    public boolean sendKeepAlive(NodePacket keepAlive) {

        boolean sent = false;

        Iterator<Map.Entry<String , NodeConnectionInfo>> iterator =
            activeConnections.entrySet().iterator();

        while(iterator.hasNext()){
            Map.Entry<String, NodeConnectionInfo> entry = iterator.next();
            sent = entry.getValue().send(keepAlive); 
        }
        return sent;
    }

    public void terminateConnection(String id) {
        NodeConnectionInfo remove = activeConnections.get(id);
        logger.info("Terminated Connection: \n  ID:" + remove.getId() 
            + "\n  IP: " + remove.getIp()
        );
        activeConnections.remove(remove.getId());
    }

    /*
     *      Mutators
     */

    // Can allow for multiple connections to be added at once(if needed)
    public void addConnection(NodeConnectionInfo... connection) {
        for(NodeConnectionInfo connected : connection) {
            activeConnections.put(connected.getId(), connected);
        }
    }

    // When recieving a 
    public void updateById(String id, Object updator) {
        NodeConnectionInfo updatee = activeConnections.get(id);

        // TODO: Figure out a system for updating

       // updatee.update(updator)
    }

    /*
     * 
     *      Getters
     * 
     */
    public NodeConnectionInfo getConnectionInfoById(String id) {
        return activeConnections.get(id);
    }

    public Map<String, NodeConnectionInfo> getActiveConnections() {
        return activeConnections;
    }

    public String[] getAllIds() {
        return activeConnections.keySet().toArray(new String[0]);
    }
}
