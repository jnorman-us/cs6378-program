import java.util.Objects;

//Class needs to be serializable since its a part of Message
public class NodeID implements java.io.Serializable
{
	//ID of the node
	private int identifier;
	
	//Constructor
	public NodeID(int id)
	{
		identifier = id;
	}
	
	//Getter function for ID
	public int getID()
	{
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
