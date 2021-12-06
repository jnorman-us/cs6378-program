package project3;

import node.Listener;
import node.Message;
import node.Node;
import node.NodeID;

import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

public class DLock implements Listener {
    private Node node;

    private NodeID id;
    private String configFile;

    private int timestamp; // scalar clock
    private boolean isLocked;
    private Payload currentRequest; // the current payload created by the lock call
    private Semaphore editingRequests;
    private PriorityQueue<Payload> requests; // queue of requests, ordered by timestamps
    private HashMap<Integer, Integer> latestTimestamps; // timestamps reported from neighbors

    public synchronized void lock() {
        // TODO
        currentRequest = new Payload(id, nextTimestamp()); // update the timestamp by 1 upon sending a message
        addToQueue(currentRequest);
        node.sendToAll(currentRequest);

        while(!conditionTimestampLesser() || !conditionCurrentAtFront()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        isLocked = true;
        // conditions pass, allow Critical Section
    }

    public void unlock() {
        isLocked = false;
        removeFromQueue(id);
        currentRequest = null;

        nextTimestamp(); // update the timestamp by 1 upon sending a message
        for(Payload request : requests) {
            Payload response = request.reply(id, timestamp);
            node.send(response, request.source);
        }
    }

    @Override
    public synchronized void receive(Message message) {
        nextTimestamp(); // update the timestamp by 1 upon receiving a message

        Payload payload = (Payload) message;
        updateTimestamp(payload);

        if(payload.isReply()) {
            // remove it from the queue
            removeFromQueue(payload.source);
            notifyAll();
        }
        else {
            // add it to the queue
            addToQueue(payload);

            // checks if currently executing CS
            if(currentRequest == null || currentRequest.compareTo(payload) > 0) {
                nextTimestamp();
                Payload response = payload.reply(id, timestamp);
                node.send(response, payload.source);
            }
        }
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
        isLocked = false;
        currentRequest = null;
        editingRequests = new Semaphore(1);
        requests = new PriorityQueue<>();
        latestTimestamps = new HashMap<>();

        for(NodeID neighbor : node.getNeighbors()) {
            latestTimestamps.put(neighbor.getID(), 0);
        }
    }

    public void addToQueue(Payload request) {
        try {
            editingRequests.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        requests.add(request);
        editingRequests.release();
    }

    public void removeFromQueue(NodeID id) {
        try {
            editingRequests.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(Iterator<Payload> iterator = requests.iterator(); iterator.hasNext(); ) {
            Payload p = iterator.next();
            if(p.source.getID() == id.getID()) {
                if(currentRequest == null || currentRequest.compareTo(p) >= 0) {
                    iterator.remove();
                }
            }
        }
        editingRequests.release();
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

    public NodeID getID() {
        return id;
    }

    public void close() {
        node.tearDown();
    }
}
