
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;
//import ConfigParser;
//import Neighbor;
//import Receiver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.System;




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
        } catch (IndexOutOfBoundsException err){
            throw new Error(err);
        } catch (IllegalArgumentException e) {
            throw new Error("Node failed to initialize because of an improperly formatted config file!");
        }


        // setup the listening server
        try {
            serverChannel = new ServerSocket(config.getOwnerDetails().getPort());
            receivers = new ArrayList<>();
        } catch (IOException e) {
            throw new Error("Node failed to initialize; IOException: \n" + e);
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
                e.printStackTrace();
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
           
            // then all the neighbor connections
            for(Neighbor neighbor : config.getNeighbors().values()) {
                neighbor.stop();
                application.broken(neighbor.getID());
            }

            // close all receivers as well
            for(Receiver receiver : receivers) {
                receiver.stop();
            }
            receivers.clear();

            //serverChannel.close();
            

        } catch(IOException e) {
            throw new Error("Node failed to teardown");
        }
    }

    public ConfigParser getConfig() {
        return config;
    }
}
