
//import Listener;
//import Message;
//import Node;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Receiver implements Runnable {
    private Node node;

    private Thread thread;
    private Socket channel;
    private AtomicBoolean running;

    private InputStream input;
    private ObjectInputStream reader;

    public Receiver(Socket channel, Node node) throws IOException {
        this.node = node;

        this.channel = channel;
        this.thread = new Thread(this);
        this.running = new AtomicBoolean(false);
        this.input = this.channel.getInputStream();
        this.reader = new ObjectInputStream(this.input);
        this.start();
    }

    public void start() {
        running.set(true);
        thread.start();
    }

    @Override
    public void run() {
        Message parsed = null;
        while(running.get()) {
            try {
                parsed = (Message)reader.readObject();
                // stop if channel was closed...
                if(parsed == null) stop();
                node.receiveMessage(parsed);
            } catch (IOException | ClassNotFoundException e) {
                try {
                    Thread.sleep(200);
                } catch(InterruptedException ex) {

                }
            }
        }
        // report node broken
        node.removeReceiver(this);
    }

    public void stop() throws IOException {
        running.set(false);
        channel.close();
    }
}
