/*
 *      Author: Nathaniel Brewer
 * 
 *      This is where all the ConnectionInfo objects will be stored and where the storage wise checks will be had for
 *      expired keepAlive.
 * 
 *      This is desined as a Singleton Pattern, to prevent multiple instances of this connection map.
 *      
 */

package connection;

import java.util.Iterator;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import packet.AbstractPacket;
import packet.keep_alive.KeepAliveManager;

public abstract class ConnectionManager {

    protected final ConcurrentHashMap<String, ConnectionDto> activeConnections = new ConcurrentHashMap<>();
    protected final String instanceId; // ID of the instance that created this connection manager
    protected final String clusterId; // ID of the cluster this instance is assigned to
    protected final String role;

    private static final Logger logger = LogManager.getLogger(ConnectionManager.class);

    /** 
     *  Subclass will NEED to implement a static getInstance() Method with sunchronized initialization
     * 
     * @param instanceId The instance that has created this manager
     * @param clusterId The id of the cluster
     * @param role What role instantiated this manager (i.e. "Server", "Node", "Coordinator")
     */
    protected ConnectionManager(String instanceId, String clusterId, String role) {
        this.instanceId = instanceId;
        this.clusterId = clusterId;
        this.role = role;
    }

    /*
            Singleton Helper - Thread-safe instance management
     */

    /**
     * Helper method for subclasses to implement thread-safe singleton pattern.
     * Each subclass should use this pattern:
     * <pre>
     * private static volatile YourConnectionManager instance;
     * 
     * public static YourConnectionManager getInstance(String instanceId, String clusterId, String role) {
     *     return getOrCreateInstance(instance, YourConnectionManager.class, 
     *         () -> instance = new YourConnectionManager(instanceId, clusterId, role));
     * }
     * </pre>
     */
    protected static <T extends ConnectionManager> T getOrCreateInstance(
            T currentInstance, 
            Class<?> lockClass, 
            java.util.function.Supplier<T> creator) {
        
        if (currentInstance == null) {
            synchronized (lockClass) {
                if (currentInstance == null) {
                    return creator.get();
                }
            }
        }
        return currentInstance;
    }

    /*
            Abstract Methods
     */

    public abstract boolean sendKeepAlive();

    /*
            End abstraction
    */
    
    public void checkExpiredConnections() {
        // Create an iterator for the ConccurentHashMap since it needs an iterator
        Iterator<Map.Entry<String, ConnectionDto>> iterator =
            activeConnections.entrySet().iterator();

        while(iterator.hasNext()) {
            // Check if each entry is expired, if it is, check the priority to see if it needs to be removed or kept alive
            Map.Entry<String, ConnectionDto> entry = iterator.next();
            if(new ConnectionDtoManager(entry.getValue()).isExpired()){

                KeepAliveManager manager = new KeepAliveManager(instanceId, clusterId, entry.getValue().getId(), null);
                
                if(entry.getValue().getPriority() == Priority.CRITICAL){

                    AbstractPacket keepAliveProbe =  manager.createOutgoingPacket();

                    boolean sendSuccess = new ConnectionDtoManager(entry.getValue()).send( keepAliveProbe );
                    if(!sendSuccess) {
                        logger.warn("Connection: \"{}\" has been terminated due to failed retry!", entry.getValue().getId());
                        terminateConnection(entry.getValue().getId());
                    }
                }
                else {
                    logger.warn("Connection: \"{}\" has been terminated due to priority status!", entry.getValue().getId()); 
                    terminateConnection(entry.getValue().getId());
                }
            }
        }
    }

    /*
     *      Response to expiry
     */

    public void terminateConnection(String id) {
        ConnectionDto remove = activeConnections.get(id);
        logger.info("Terminated Connection: \n  ID:" + remove.getId() 
            + "\n  IP - " + remove.getIp() + ":" + remove.getPort()
        );
        activeConnections.remove(remove.getId());

    }


    // Can allow for multiple connections to be added at once(if needed)
    public void addConnection(ConnectionDto... connection) {
        for(ConnectionDto connected : connection) {
            activeConnections.put(connected.getId(), connected);
        }
    }


    /*
     *      Getters
     */
    public String getInstanceId() { return instanceId; }

    public String getClusterId() { return clusterId; }

    public String getRole() { return role; }

    public ConnectionDto getConnectionInfoById(String id) { return activeConnections.get(id); }

    public ConcurrentHashMap<String, ConnectionDto> getActiveConnections() { return activeConnections; }

    public String[] getAllIds() { return activeConnections.keySet().toArray(new String[0]); }

    public int getActiveConnectionCount() {

        int connectionCount = 0;

        Iterator<Map.Entry<String, ConnectionDto>> iterator =
            activeConnections.entrySet().iterator();

        while(iterator.hasNext()){
            connectionCount++;
        }

        return connectionCount;
    }


}
