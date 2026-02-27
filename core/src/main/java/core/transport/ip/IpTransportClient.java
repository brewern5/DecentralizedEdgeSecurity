/*
    Author: Nathaniel Brewer

    Concrete implementation of transport client for the IP. 
*/
package core.transport.ip;

import core.packet.AbstractPacket;
import core.sender.PacketSender;
import core.transport.TransportClient;

public class IpTransportClient implements TransportClient {

    private final PacketSender sender;

    public IpTransportClient(String ip, int port) {
        this.sender = new PacketSender(ip, port);
    }

    @Override
    public boolean send(AbstractPacket packet) {
        return sender.send(packet);
    }

    @Override
    public boolean retry(AbstractPacket packet) {
        return sender.retry(packet);
    }

    @Override
    public String getAssignedId() {
        return sender.getAssignedId();
    }
}
