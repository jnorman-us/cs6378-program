package node;

import node.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Node implements Runnable {
    private ConfigParser config;
    private Listener application;

    private Thread thread;
    private AtomicBoolean running;
    private ServerSocket serverChannel;
    private ArrayList<Receiver> receivers;

    public Node(NodeID identifier, String configFilePath, Listener application) {
        this.application = application;
        try {
            config = new ConfigParser(identifier, this, configFilePath);
        } catch (FileNotFoundException e) {
            throw new Error("node.Node failed to initialize because of a missing config file!");
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            throw new Error("node.Node failed to initialize because of an improperly formatted config file!");
        }


        // setup the listening server
        try {
            serverChannel = new ServerSocket(config.getOwnerDetails().getPort());
            receivers = new ArrayList<>();
        } catch (IOException e) {
            throw new Error("node.Node failed to initialize; IOException");
        }
        thread = new Thread(this);
        running = new AtomicBoolean(false);
        this.start();
        
        // setup the client connections to each neighbor
        for(Neighbor neighbor : config.getNeighbors().values()) {
            neighbor.start();
        }
    }

    public void start() {
        running.set(true);
        thread.start();
    }

    @Override
    public void run() {
        while(running.get()) {
            try {
                // open up channels for each connecting client
                Socket channel = serverChannel.accept();
                Receiver receiver = new Receiver(channel, this);
                receivers.add(receiver);
            } catch(IOException e) {
            }
        }
    }

    public void receiveMessage(Message message) {
        application.receive(message);
    }

    public void removeReceiver(Receiver receiver) {
        receivers.remove(receiver);
    }

    public void reportNeighborBroken(Neighbor neighbor) {
        application.broken(new NodeID(neighbor.getID()));
    }

    public NodeID[] getNeighbors() {
        Set<Integer> neighborNodeIDs = config.getNeighbors().keySet();
        NodeID[] neighbors = new NodeID[neighborNodeIDs.size()];

        int i = 0;
        for(int neighborNodeID : neighborNodeIDs) {
            neighbors[i] = new NodeID(neighborNodeID);
            i ++;
        }

        return neighbors;
    }

    public void send(Message message, NodeID destination) throws IndexOutOfBoundsException {
        int destinationID = destination.getID();
        if(config.getNeighbors().containsKey(destinationID)) {
            config.getNeighbors().get(destinationID).send(message);
        }
        else {
            throw new IndexOutOfBoundsException();
        }
    }

    public void sendToAll(Message message) {
        for(Neighbor neighbor : config.getNeighbors().values()) {
            neighbor.send(message);
        }
    }

    public void tearDown() {
        running.set(false);
        try {
            // then all the neighbor connections
            for(Neighbor neighbor : config.getNeighbors().values()) {
                try {
                    neighbor.stop();
                } catch (NullPointerException e){
                    //do nothing
                    //already closed
                }
            }

            // close all receivers as well
            for(Receiver receiver : receivers) {
                receiver.stop();
            }
            receivers.clear();
            serverChannel.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public ConfigParser getConfig() {
        return config;
    }
}
