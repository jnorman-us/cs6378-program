//package node;

//import node.NodeID;


//node.Message needs to be serializable in order to send it using sockets
public class Message implements java.io.Serializable
{
	//ID of the node sending the message
	public NodeID source;
	
	//Payload of the message
	public byte[] data;
	
	//Constructor
	public Message(NodeID source, byte[] data)
	{
		this.source = source;
		this.data = data;
	}
}
