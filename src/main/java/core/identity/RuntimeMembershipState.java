/*
    Author: Nathaniel Brewer

    Where the identity given to each memeber is stored as well as the id of the cluster they belong to 
*/
package core.identity;

public final class RuntimeMembershipState {
    
    private volatile String assignedId;
    private volatile String clusterId;

    public String assignedId() { return assignedId; }
    public void assignId(String assignedId) { this.assignedId = assignedId; }

    public String clusterId() { return clusterId; }
    public void assignClusterId(String clusterId) { this.clusterId = clusterId; }
}
