import node.NodeID;
import test.Application;

import java.io.IOException;

public class Main
{
	public static void main(String[] args)
	{
		//Read node.NodeID and Config file from command line
		NodeID id = new NodeID(Integer.parseInt(args[0]));
		String configFile = args[1];



		//Launch application and wait for it to terminate
		Application myApp = new Application(id, configFile);
		myApp.run();

		/*try {
			myApp.printNodes(new NodeID[][]{
				{ new NodeID(1), new NodeID(3) },
				{ new NodeID(0), new NodeID(4) },
				{},
				{},
			}, "output.txt");
		} catch (IOException e) {
			e.printStackTrace();
		} */
	}
}

