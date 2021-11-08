package sendall;

import node.Message;
import node.NodeID;

import java.nio.charset.StandardCharsets;

public class Payload extends Message {
    public final static String PAYLOAD_MESSAGE = "**PAYLOAD**";
    public final static byte[] PAYLOAD_BYTES = PAYLOAD_MESSAGE.getBytes(StandardCharsets.UTF_8);

    boolean response;

    public Payload(NodeID sender) {
        super(sender, PAYLOAD_BYTES);

        response = false;
    }

    public boolean isOriginal() {
        return !response;
    }

    public void respond(NodeID responder) {
        response = true;
        source = responder;
    }
}
