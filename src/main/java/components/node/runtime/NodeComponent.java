/*
    Author: Nathaniel Brewer

    This is the concrete implementation of the EdgeComponent. 
*/
package components.node.runtime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import components.node.identity.NodeIdentity;
import components.node.listener.NodeListener;
import components.node.config.NodeConfig;

import components.node.connections.NodeServerConnectionManager;

import core.config.AbstractConfig;

import core.connection.Priority;
import core.connection.ConnectionDto;
import core.connection.ConnectionManager;

import core.identity.AbstractTierIdentity;
import core.identity.TierRole;
import core.packet.AbstractPacket;
import core.packet.initalization.InitalizationPacketManager;
import core.runtime.AbstractEdgeComponent;

public final class NodeComponent extends AbstractEdgeComponent {

    private static NodeListener serverListener;

    private static final Logger logger = LogManager.getLogger(NodeComponent.class);
    @Override
    protected Logger getLogger() { return logger; }

    @Override
    protected void validateStartupArgs(String[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            logger.error("Node requires instanceId argument!");
            throw new IllegalArgumentException("Node requires instanceId argument");
        }
    }

    @Override
    protected AbstractTierIdentity buildIdentity(String[] args) {
        return new NodeIdentity(TierRole.NODE, TierRole.SERVER, args[0]);
    }

    @Override 
    protected AbstractConfig loadConfig(AbstractTierIdentity identity) {
        return new NodeConfig(identity);
    }

    @Override
    protected void refreshRuntimeIp() {
        try{
            config.grabIP();
        } catch (SocketException se){
            logger.error("Exception thrown for socket creations!", se);
        } catch(UnknownHostException uhe) {
            logger.error("Could not determine machine IP!", uhe);
        }
    }

    @Override
    protected void initializeConnections() {

        try {
            connectionManagers.put(
                identity.getHigherTier(), 
                NodeServerConnectionManager.getInstance(membershipState, identity.getRole())
            );

            ConnectionManager serverConnection = connectionManagers.get(identity.getHigherTier());

            serverConnection.addConnection(new ConnectionDto(
                "1",
                config.getIPByKey("Server.IP"),
                config.getPortByKey("Server.listeningPort"),
                Priority.CRITICAL
            ));
            
            LinkedHashMap<String, String> payload = new LinkedHashMap<>();
            payload.put(
                "Node.listeningPort",
                String.valueOf(config.getPortByKey("Node.listeningPort")) 
            );

            AbstractPacket initPacket = new InitalizationPacketManager(
                membershipState,
                "1",
                identity.getRole(),
                serverConnection,
                config.getIPByKey("Node.IP")
            )
            .createOutgoingPacket();

            initPacket.addPayload(payload);

            serverConnection.sendToConnection("1", initPacket);

        } catch(Exception e) {
            logger.error("Error Sending Initalization Packet: " + e);
        }

    }

    @Override
    protected void startListeners() {

        int timeoutMs = 2000; 

        try {
            serverListener = new NodeListener(
                config.getPortByKey("Node.listeningPort"), 
                timeoutMs,
                identity,
                membershipState
            );

            listenerExecutor.execute(serverListener);
            logger.info("Node listener submitted on port {}", serverListener.getActivePort());
        } catch (Exception e) {
            logger.error(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("Node.listeningPort")
                + e
            );
            // TODO: try to grab new port if this one is unavailable
        }
    }

    @Override
    protected void scheduleTimers() {
        try{
            if (scheduler == null) {
                throw new IllegalStateException("Scheduler is not initialized before scheduleTimers()");
            }

            scheduler.scheduleAtFixedRate(() ->{
                try {
                    boolean keepAliveSent;

                    keepAliveSent = connectionManagers.get(identity.getHigherTier()).sendKeepAlive();

                    if(!keepAliveSent){
                        logger.error("Keep alive was not sent!");
                    }

                } catch(core.exception.KeepAliveException e) {
                    // Detailed logging for keep-alive specific failures
                    logger.error("Keep-alive failed at stage: {} for connection: {} - {}", 
                               e.getStage(), e.getConnectionId(), e.getMessage());
                
                    // Different handling based on failure type
                    if (e.isSendFailure()) {
                        logger.error("Failed to send keep-alive packet - network or socket issue");
                    } else if (e.isAckFailure()) {
                        if (e.getStage() == core.exception.KeepAliveException.FailureStage.ACK_NOT_RECEIVED) {
                            logger.error("Keep-alive sent but ACK not received - {} may be down", identity.getHigherTier());
                        } else if (e.getStage() == core.exception.KeepAliveException.FailureStage.ACK_NOT_HANDLED) {
                            logger.error("ACK received but not handled properly - handler issue");
                        }
                    }
                }
            }, 5, 30, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Unhandled Exception when creating Scheduler!", e);
        }
        
    }

    @Override
    protected void beforeStop() {
        if (serverListener != null) {
            serverListener.closeSocket();
        }
    }

    @Override
    protected void afterStart() {
        // TODO: PeerList request
    }
}