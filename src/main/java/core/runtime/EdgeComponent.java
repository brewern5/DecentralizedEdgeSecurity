/*
    Author: Nathaniel Brewer

    This is the interface for the lifecycle contract of each component. This is the main interface of the each component. 

*/
package core.runtime;

public interface EdgeComponent {
    void start(String[] args);
    void stop();
    ComponentStatus status();
    boolean health();
}
