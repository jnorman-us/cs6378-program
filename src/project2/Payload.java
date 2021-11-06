package project2;

import node.Message;
import node.NodeID;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Payload extends Message {
    public final static String PAYLOAD_MESSAGE = "**PAYLOAD**";
    public final static byte[] PAYLOAD_BYTES = PAYLOAD_MESSAGE.getBytes(StandardCharsets.UTF_8);

    public int round;
    public NodeID[] kHopNeighbors;

    public Payload(int round, NodeID source, NodeID[] kHopNeighbors) {
        super(source, PAYLOAD_BYTES);
        this.round = round;
        this.kHopNeighbors = kHopNeighbors;
    }

    // FOR PRINT DEBUGGING
    /*
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Received NodeID:");
        sb.append(source.getID());
        sb.append(";Round:");
        sb.append(round);
        sb.append("> ");
        for(NodeID neighbor : kHopNeighbors) {
            sb.append(neighbor.getID());
            sb.append(",");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }
    */
}