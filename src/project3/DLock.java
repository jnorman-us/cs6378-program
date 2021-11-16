package project3;

import node.Listener;
import node.Message;
import node.Node;
import node.NodeID;

import java.util.HashMap;
import java.util.PriorityQueue;

public class DLock implements Listener {
    private Node node;

    private NodeID id;
    private String configFile;

    private int timestamp; // scalar clock
    private Payload currentRequest; // the current payload created by the lock call
    private PriorityQueue<Payload> requests; // queue of requests, ordered by timestamps
    private HashMap<Integer, Integer> latestTimestamps; // timestamps reported from neighbors

    public void lock() {
        // TODO
    }

    public void unlock() {
        // TODO
    }

    @Override
    public void receive(Message message) {
        // TODO
    }

    @Override
    public void broken(NodeID neighbor) {
        // TODO
    }

    // constructor
    public DLock(NodeID id, String configFile) {
        this.id = id;
        this.configFile = configFile;

        node = new Node(id, configFile, this);

        timestamp = 0;
        currentRequest = null;
        requests = new PriorityQueue<>();
        latestTimestamps = new HashMap<>();

        for(NodeID neighbor : node.getNeighbors()) {
            latestTimestamps.put(neighbor.getID(), 0);
        }
    }

    // method for incrementing timestamp each time a new event occurs
    // i.e. when a send occurs and a timestamp needs to be put into the
    // payload
    public int nextTimestamp() {
        return timestamp ++;
    }

    // method for updating timestamp when a Payload is received with a
    // timestamp from another process
    public void updateTimestamp(Payload p) {
        int incomingTimestamp = p.getTimestamp();
        latestTimestamps.put(p.source.getID(), p.getTimestamp());
        timestamp = Math.max(incomingTimestamp, timestamp);
    }

    // checks the first condition of executing the Critical Section:
    // the process must have received messages with timestamps greater
    // than that of the currentRequest
    public boolean conditionTimestampLesser() {
        if(currentRequest == null) return false;

        for(Integer latest : latestTimestamps.values()) {
            if(latest <= currentRequest.getTimestamp())
                return false;
        }
        return true;
    }

    // checks the second condition of executing the Critical Section:
    // the currentRequest must be at the front of the queue
    public boolean conditionCurrentAtFront() {
        if(requests.isEmpty()) return false;
        if(currentRequest == null) return false;

        return requests.peek().equals(currentRequest);
    }

    public void close() {
        node.tearDown();
    }
}
