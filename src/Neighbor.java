import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Neighbor implements Runnable {
    private Node owner;
    private int id;

    private String hostname;
    private int port;

    private Thread thread;
    private AtomicBoolean running;
    private LinkedList<Message> toSend;

    private Socket channel;
    private OutputStream output;
    private ObjectOutputStream writer;

    public Neighbor(int id, String hostname, int port, Node owner) {
        this.id = id;
        this.hostname = hostname;
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
        StillAliveMessage alive = new StillAliveMessage(new NodeID(owner.getConfig().getOwner()));
        boolean scanning = true;

        // first scan for the server to attempt to open connection
        while(running.get() && scanning) {
            try {
                channel = new Socket(getIP(), getPort());
                output = channel.getOutputStream();
                writer = new ObjectOutputStream(output);
                scanning = false;
            } catch (IOException e) {
                try {
                    Thread.sleep(500);
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
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch(IOException e) {
            // writer closed, socket closed...
            // exit out of loop because disconnected
            running.set(false);
        }
        owner.reportNeighborBroken(this);
    }

    public void stop() throws IOException {
        running.set(false);
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

    public int getID() {
        return id;
    }

    public String getIP() {
        return  "127.0.0.1"; // hostname + ".utdallas.edu";
    }

    @Override
    public String toString() {
        return "Neighbor " + getID() + "; Hostname: " + hostname + "; Port: " + port;
    }
}
