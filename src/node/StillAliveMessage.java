package node;

import node.NodeID;

import java.nio.charset.StandardCharsets;

public class StillAliveMessage extends Message {
    public final static String ALIVE_MESSAGE = "**ALIVE**";

    public StillAliveMessage(NodeID source) {
        super(source, ALIVE_MESSAGE.getBytes(StandardCharsets.UTF_8));
    }
}
