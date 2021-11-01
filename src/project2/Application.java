package project2;

import node.Listener;
import node.Message;
import node.NodeID;

import java.io.*;

public class Application implements Listener {
    public Application(NodeID identifier, String configFile) {
        Payload before = new Payload(3, identifier, new NodeID[]{ new NodeID(3), new NodeID(4) });
        System.out.println(before);

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(before);
            before = null;
            byte[] payloadBytes = bos.toByteArray();
            
            ByteArrayInputStream bis = new ByteArrayInputStream(payloadBytes);
            ObjectInputStream in = new ObjectInputStream(bis);
            Payload afterwards = (Payload) in.readObject();
            System.out.println(afterwards);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void run() {

    }

    @Override
    public void receive(Message message) {

    }

    @Override
    public void broken(NodeID neighbor) {

    }
}
