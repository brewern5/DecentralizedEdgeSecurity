/*
    Author: Nathaniel Brewer

    We chose an interface for the transport layer since further extensions way need to
    implement sub-interfaces for functionality
*/
package core.transport;

import core.packet.AbstractPacket;

public interface TransportClient {

    boolean send(AbstractPacket packet);

    boolean retry(AbstractPacket packet);

    String getAssignedId();
}
