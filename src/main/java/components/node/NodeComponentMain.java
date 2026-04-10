package components.node;

import components.node.runtime.NodeComponent;

public final class NodeComponentMain {
    public static void main(String[] args) {
        NodeComponent component = new NodeComponent();
        component.start(args);
    }
}
