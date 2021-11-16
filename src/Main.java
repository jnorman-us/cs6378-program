import node.NodeID;
import project3.Application;
import project3.Payload;

import java.util.PriorityQueue;

public class Main
{
	public static void main(String[] args)
	{
		//Read node.NodeID and Config file from command line
		NodeID id = new NodeID(Integer.parseInt(args[0]));
		String configFile = args[1];
		int aird = Integer.parseInt(args[2]);
		int acset = Integer.parseInt(args[3]);
		int ncsr = Integer.parseInt(args[4]);

		//Launch application and wait for it to terminate
		Application myApp = new Application(id, configFile, aird, acset, ncsr);
		myApp.run();
	}
}

