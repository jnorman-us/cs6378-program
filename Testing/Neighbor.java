

//import Message;
//import Node;
//import NodeID;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Neighbor implements Runnable {
    private Node owner;
    private NodeID id;

    private String hostname;
    private int port;

    private Thread thread;
    private AtomicBoolean running;
    private LinkedList<Message> toSend;

    private Socket channel;
    private OutputStream output;
    private ObjectOutputStream writer;

    public Neighbor(NodeID id, String hostname, int port, Node owner) {
        this.id = id;
        this.hostname = hostname + ".utdallas.edu";
        this.port = port;
        this.owner = owner;

        this.thread = new Thread(this);
        this.running = new AtomicBoolean(false);
        this.toSend = new LinkedList<>();
        this.channel = new Socket();
    }

    public void send(Message message) {
        toSend.add(message);
    }

    public void start() {
        running.set(true);
        thread.start();
    }

    @Override
    public void run() {
        boolean scanning = true;
        // first scan for the server to attempt to open connection
        while(running.get() && scanning) {
            try {
                channel = new Socket(getHostname(), getPort());
                output = channel.getOutputStream();
                writer = new ObjectOutputStream(output);
                scanning = false;
            } catch (IOException e) {
                System.out.println("Failed to connect, retrying...");
             
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        while(running.get()) {
            while(!toSend.isEmpty()) {
                Message message = toSend.poll();
                try {
                    writer.writeObject(message);
                } catch (IOException e) {
                    try {
                        stop();
                        owner.reportNeighborBroken(this);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() throws IOException {
        running.set(false);
        channel.close();
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

    public NodeID getID() {
        return id;
    }

    public String getHostname(){
        return hostname;
    }

    public String getIP() {
        return  "127.0.0.1"; // hostname + ".utdallas.edu";
    }

    @Override
    public String toString() {
        return "" + id.toString() + "; Hostname:" + hostname + "; Port:" + port;
    }
}
