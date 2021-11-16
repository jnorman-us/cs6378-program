package project3;

import node.Message;
import node.NodeID;

import java.nio.charset.StandardCharsets;

public class Reply extends Message {
    public static final String REPLY_MESSAGE = "**REPLY**";
    public static final byte[] REPLY_BYTES = REPLY_MESSAGE.getBytes(StandardCharsets.UTF_8);

    public Reply(NodeID source) {
        super(source, REPLY_BYTES);
    }
}
