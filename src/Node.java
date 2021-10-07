import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
            throw new Error("Node failed to initialize because of a missing config file!");
        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            throw new Error("Node failed to initialize because of an improperly formatted config file!");
        }


        // setup the listening server
        try {
            serverChannel = new ServerSocket(config.getOwnerDetails().getPort());
            receivers = new ArrayList<>();
        } catch (IOException e) {
            throw new Error("Node failed to initialize; IOException");
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
        System.out.println(message);
        application.receive(message);
    }

    public void removeReceiver(Receiver receiver) {
        receivers.remove(receiver);
    }

    public void reportNeighborBroken(Neighbor neighbor) {
        application.broken(neighbor.getID());
    }

    public NodeID[] getNeighbors() {
        return config.getNeighbors().keySet().toArray(new NodeID[]{});
    }

    public void send(Message message, NodeID destination) throws IndexOutOfBoundsException {
        if(config.getNeighbors().containsKey(destination)) {
            config.getNeighbors().get(destination).send(message);
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
            serverChannel.close();

            // close all receivers as well
            for(Receiver receiver : receivers) {
                receiver.stop();
            }
            receivers.clear();

            // then all the neighbor connections
            for(Neighbor neighbor : config.getNeighbors().values()) {
                neighbor.stop();
            }
        } catch(IOException e) {
            throw new Error("Node failed to teardown");
        }
    }

    public ConfigParser getConfig() {
        return config;
    }
}
