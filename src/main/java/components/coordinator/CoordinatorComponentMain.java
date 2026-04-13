package components.coordinator;

import components.coordinator.runtime.CoordinatorComponent;

public class CoordinatorComponentMain {
    public static void main(String[] args) {
        CoordinatorComponent component = new CoordinatorComponent();
        component.start(args);
    }
}
