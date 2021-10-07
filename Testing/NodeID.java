

import java.io.Serializable;
import java.util.Objects;

public class NodeID implements Serializable {
    private int identifier;

    public NodeID(int identifier) {
        this.identifier = identifier;
    }

    // no setter LMAO
    public int getID() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeID nodeID = (NodeID) o;
        return identifier == nodeID.identifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return "NodeID:" + identifier;
    }
}
