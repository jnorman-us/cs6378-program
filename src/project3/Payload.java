package project3;

import node.Message;
import node.NodeID;

import java.nio.charset.StandardCharsets;

public class Payload extends Message implements Comparable {
    public static final String REQUEST_MESSAGE = "**REQUEST**";
    public static final byte[] REQUEST_BYTES = REQUEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

    private int timestamp;
    private boolean reply;

    public Payload(NodeID source, int timestamp) {
        super(source, REQUEST_BYTES);

        this.timestamp = timestamp;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public boolean isReply() {
        return reply;
    }

    public Payload reply(NodeID source, int timestamp) {
        Payload reply = new Payload(source, timestamp);
        reply.reply = true;
        return reply;
    }

    @Override
    public int compareTo(Object o) {
        Payload other = (Payload) o;

        int timestampDiff = timestamp - other.timestamp;
        int idDiff = source.getID() - other.source.getID();

        if(timestampDiff == 0) {
            if(idDiff == 0) {
                return 0;
            }
            return idDiff;
        }
        return timestampDiff;
    }

    @Override
    public boolean equals(Object o) {
        return compareTo(o) == 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(isReply() ? "Reply" : "Request");
        sb.append(": <timestamp");
        sb.append(timestamp).append(", ");
        sb.append("node").append(source.getID());
        sb.append(">");
        return sb.toString();
    }
}
