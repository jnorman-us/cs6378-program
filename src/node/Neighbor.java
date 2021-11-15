package node;

import node.NodeID;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Neighbor implements Runnable {
    private Node owner;
    private int id;

    private String hostname;
    private int port;

    private Thread thread;
    private AtomicBoolean running;
    private AtomicBoolean connected;
    private LinkedList<Message> toSend;

    private Socket channel;
    private OutputStream output;
    private ObjectOutputStream writer;

    public Neighbor(int id, String hostname, int port, Node owner) {
        this.id = id;
        this.hostname = "127.0.0.1"; //hostname + ".utdallas.edu";
        this.port = port;
        this.owner = owner;

        this.thread = new Thread(this);
        this.running = new AtomicBoolean(false);
        this.connected = new AtomicBoolean(false);
        this.toSend = new LinkedList<>();
        this.channel = new Socket();
    }

    public void send(Message message) {
        // this is a change I made to the neighbor class that I thought
        // would cause a race condition between sending a message and receiving
        // everything and shutting down. This was intended to block the send
        // until the connection was fully setup
        /*if(!running.get()) {
            return;
        }

        while(!connected.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        toSend.add(message);
    }

    public void start() {
        running.set(true);
        thread.start();
    }

    @Override
    public void run() {
        StillAliveMessage alive = new StillAliveMessage(new NodeID(owner.getConfig().getOwner()));
        boolean scanning = true;

        // first scan for the server to attempt to open connection
        while(running.get() && scanning) {
            try {
                channel = new Socket(getHostname(), getPort());
                output = channel.getOutputStream();
                writer = new ObjectOutputStream(output);
                scanning = false;
                connected.set(true);
            } catch (IOException e) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        try {
            while(running.get()) {
                writer.writeObject(alive);
                while(!toSend.isEmpty()) {
                    Message message = toSend.poll();
                    writer.writeObject(message);
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch(IOException e) {
            // writer closed, socket closed...
            // exit out of loop because disconnected
            if(running.get()) {
                connected.set(false);
                running.set(false);
            }
        }
        owner.reportNeighborBroken(this);
    }

    public void stop() throws IOException {
        running.set(false);
        connected.set(false);
        try {
            channel.close();
            output.close();
            writer.close();
        } catch(IOException e) {
            // do nothing, it has already closed.
        }
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }


    public String getHostname(){
        return hostname;
    }
    
    public int getID() {
        return id;
    }

    public String getIP() {
        return  "127.0.0.1"; // hostname + ".utdallas.edu";
    }

    @Override
    public String toString() {
        return "node.Neighbor " + getID() + "; Hostname: " + hostname + "; Port: " + port;
    }
}
