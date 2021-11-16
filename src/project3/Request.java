package project3;

import node.Message;
import node.NodeID;

import java.nio.charset.StandardCharsets;

public class Request extends Message {
    public static final String REQUEST_MESSAGE = "**REPLY**";
    public static final byte[] REQUEST_BYTES = REQUEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

    public Request(NodeID source) {
        super(source, REQUEST_BYTES);
    }
}
