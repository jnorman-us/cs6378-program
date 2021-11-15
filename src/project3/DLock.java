package project3;

import node.Listener;
import node.Message;
import node.Node;
import node.NodeID;

public class DLock implements Listener {
    private Node node;

    private NodeID id;
    private String configFile;

    public DLock(NodeID id, String configFile) {
        this.id = id;
        this.configFile = configFile;

        this.node = new Node(id, configFile, this);
    }

    public void lock() {

    }

    public void unlock() {

    }

    @Override
    public void receive(Message message) {

    }

    @Override
    public void broken(NodeID neighbor) {

    }

    public void close() {
        node.tearDown();
    }
}
