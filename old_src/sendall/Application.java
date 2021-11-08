package sendall;

import node.Listener;
import node.Message;
import node.Node;
import node.NodeID;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class Application implements Listener {
    private Node node;

    private NodeID id;
    private String configFile;

    private Semaphore responses;
    private Semaphore broken;

    public Application(NodeID id, String configFile) {
        this.id = id;
        this.configFile = configFile;
    }

    public void run() {
        Payload original = new Payload(id);

        node = new Node(id, configFile, this);
        NodeID[] neighbors = node.getNeighbors();

        responses = new Semaphore(-1 * neighbors.length + 1);
        broken = new Semaphore(-1 * neighbors.length + 1);

        node.sendToAll(original);

        // acquire all of the leases
        try {
            responses.acquire(); // they all respond, then continue
        } catch(InterruptedException e) {

        }
        System.out.println("All responses received, tearing down");
        node.tearDown();

        try {
            broken.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Finished");
    }

    @Override
    public void receive(Message message) {
        Payload p = (Payload) message;
        NodeID source = p.source;

        if(p.isOriginal()) {
            p.respond(id);
            System.out.println("B: " + id.getID() + " -> "  + source.getID());
            node.send(p, source);
        }
        else {
            System.out.println("R: " + id.getID() + " <- " + source.getID());
            responses.release();
        }
    }

    @Override
    public void broken(NodeID neighbor) {
        System.out.println("Broken pipe with " + neighbor.getID());
        broken.release();
    }
}
