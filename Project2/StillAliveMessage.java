//package node;

//import node.NodeID;

import java.nio.charset.StandardCharsets;

public class StillAliveMessage extends Message {
    public final static String ALIVE_MESSAGE = "**ALIVE**";
    public final static byte[] ALIVE_BYTES = ALIVE_MESSAGE.getBytes(StandardCharsets.UTF_8);

    public StillAliveMessage(NodeID source) {
        super(source, ALIVE_BYTES);
    }
}
