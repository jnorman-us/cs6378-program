//package node;

//import node.Message;
//import node.StillAliveMessage;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class Receiver implements Runnable {
    public Node node;

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
        try {
            while (running.get()) {
                parsed = (Message) reader.readObject();
                if(parsed == null) throw new IOException();

                String dataString = new String(parsed.data, StandardCharsets.UTF_8);

                if(!dataString.equals(StillAliveMessage.ALIVE_MESSAGE)) { // check if message is not the node.StillAliveMessage
                    node.receiveMessage(parsed);
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {

                }
            }
        } catch(IOException | ClassNotFoundException e) {
            if(running.get()) {
                running.set(false);
                node.removeReceiver(this);
            }
        }
        // report node broken
    }

    public void stop() {
        running.set(false);
        try {
            channel.close();
            input.close();
            reader.close();
        } catch(IOException e) {
            // do nothing. perhaps it was already closed
        }
    }
}
