package core.transport;

import core.connection.ConnectionDto;

public interface TransportPlugin {

    String name();

    boolean supports(TransportMode mode);

    TransportClient create(ConnectionDto connection);
}
