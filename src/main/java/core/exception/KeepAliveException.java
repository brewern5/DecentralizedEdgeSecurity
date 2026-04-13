package core.exception;

/**
 * Exception thrown when a keep-alive operation fails.
 * Provides detailed information about the stage of failure to aid in debugging.
 * 
 * @author Nathaniel Brewer
 */
public class KeepAliveException extends Exception {
    

    public enum FailureStage {

        SEND_FAILED("Keep-alive packet failed to send"),
        
        ACK_NOT_RECEIVED("Keep-alive sent but ACK not received"),

        ACK_NOT_HANDLED("ACK received but not handled properly"),

        ACK_ERROR_RESPONSE("ACK received with error response"),

        TIMEOUT("Keep-alive operation timed out"),

        UNKNOWN("Unknown keep-alive failure");
        
        private final String description;
        
        FailureStage(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private final FailureStage stage;
    private final String connectionId;
    private final String instanceId;
    private final int attemptNumber;
    
    /**
     * Constructs a new KeepAliveException with detailed failure information
     * 
     * @param stage The stage at which the keep-alive failed
     * @param connectionId The ID of the connection that failed
     * @param instanceId The ID of the instance sending the keep-alive
     * @param attemptNumber The attempt number when the failure occurred
     * @param message Additional detail message
     */
    public KeepAliveException(FailureStage stage, String connectionId, String instanceId, 
                              int attemptNumber, String message) {
        super(buildMessage(stage, connectionId, instanceId, attemptNumber, message));
        this.stage = stage;
        this.connectionId = connectionId;
        this.instanceId = instanceId;
        this.attemptNumber = attemptNumber;
    }
    
    /**
     * Constructs a new KeepAliveException with a cause
     */
    public KeepAliveException(FailureStage stage, String connectionId, String instanceId, 
                              int attemptNumber, String message, Throwable cause) {
        super(buildMessage(stage, connectionId, instanceId, attemptNumber, message), cause);
        this.stage = stage;
        this.connectionId = connectionId;
        this.instanceId = instanceId;
        this.attemptNumber = attemptNumber;
    }
    
    /**
     * Simplified constructor for cases where attempt number is not relevant
     */
    public KeepAliveException(FailureStage stage, String connectionId, String instanceId, String message) {
        this(stage, connectionId, instanceId, 0, message);
    }
    
    /**
     * Simplified constructor with cause
     */
    public KeepAliveException(FailureStage stage, String connectionId, String instanceId, 
                              String message, Throwable cause) {
        this(stage, connectionId, instanceId, 0, message, cause);
    }
    
    private static String buildMessage(FailureStage stage, String connectionId, String instanceId, 
                                      int attemptNumber, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(stage.name()).append("] ");
        sb.append(stage.getDescription());
        sb.append(" | Connection: ").append(connectionId);
        sb.append(" | Instance: ").append(instanceId);
        if (attemptNumber > 0) {
            sb.append(" | Attempt: ").append(attemptNumber);
        }
        if (message != null && !message.isEmpty()) {
            sb.append(" | Details: ").append(message);
        }
        return sb.toString();
    }
    
    // Getters
    
    public FailureStage getStage() {
        return stage;
    }
    
    public String getConnectionId() {
        return connectionId;
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public int getAttemptNumber() {
        return attemptNumber;
    }
    
    /**
     * Returns true if the failure was due to sending issues (vs. receiving issues)
     */
    public boolean isSendFailure() {
        return stage == FailureStage.SEND_FAILED;
    }
    
    /**
     * Returns true if the packet was sent but ACK had issues
     */
    public boolean isAckFailure() {
        return stage == FailureStage.ACK_NOT_RECEIVED 
            || stage == FailureStage.ACK_NOT_HANDLED 
            || stage == FailureStage.ACK_ERROR_RESPONSE;
    }
}
