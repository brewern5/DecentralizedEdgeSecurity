package core.transport.ip;

import core.connection.ConnectionDto;
import core.transport.TransportClient;
import core.transport.TransportMode;
import core.transport.TransportPlugin;

public class IpTransportPlugin implements TransportPlugin {

    @Override
    public String name() {
        return "ip";
    }

    @Override
    public boolean supports(TransportMode mode) {
        return mode == TransportMode.IP;
    }

    @Override
    public TransportClient create(ConnectionDto connection) {
        return new IpTransportClient(connection.getIp(), connection.getPort());
    }
}
