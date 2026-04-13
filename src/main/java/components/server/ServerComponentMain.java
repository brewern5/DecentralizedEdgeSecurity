package components.server;

import components.server.runtime.ServerComponent;

public final class ServerComponentMain {
    public static void main(String[] args) {
        ServerComponent component = new ServerComponent();
        component.start(args);
    }
}
