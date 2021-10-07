import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class ConfigParser {
    private NodeID owner;
    private HashMap<NodeID, Neighbor> neighbors;
    private HashMap<NodeID, Neighbor> allNodes;

    public ConfigParser(
            NodeID owner,
            Node ownerNode,
            String fileName
    ) throws FileNotFoundException, IndexOutOfBoundsException, IllegalArgumentException {
        Scanner file = new Scanner(new File(fileName));
        this.owner = owner;
        neighbors = new HashMap<>();
        allNodes = new HashMap<>();

        int nodeCount = -1; // the node count, derived on the first line
        int currentNodeDetailsI = -1; // index of the current parsing node details
        int neighborhoodI = 0;

        while(file.hasNext()) {
            String line = file.nextLine();

            // remove everything after the comment symbol
            String nonComment = line.split("#")[0];

            // confirm that this line is valid
            if(nonComment.length() <= 0) continue;
            char c = nonComment.charAt(0);
            if(!(c >= '0' && c <= '9')) continue;

            // hasn't yet found the # number of nodes
            if(nodeCount == -1) {
                nodeCount = Integer.parseInt(nonComment);
                currentNodeDetailsI = 0;

                if(owner.getID() >= nodeCount) throw new IndexOutOfBoundsException();
            }
            // have found number of nodes, now parsing the ith node's details
            else if(nodeCount >= 0 && currentNodeDetailsI < nodeCount) {
                String[] iNodeDetails = nonComment.split(" ");

                int id = Integer.parseInt(iNodeDetails[0]);
                String hostname = iNodeDetails[1];
                int port = Integer.parseInt(iNodeDetails[2]);

                if(id != currentNodeDetailsI) throw new IllegalArgumentException();

                NodeID ithNode = new NodeID(id);
                Neighbor ithNeighborNode = new Neighbor(ithNode, hostname, port, ownerNode);
                allNodes.put(ithNode, ithNeighborNode);
                currentNodeDetailsI ++;
            }
            // all node details parsed, now find the neighbor listing for
            else if(currentNodeDetailsI == nodeCount) {
                // check if current line contains the neighbors for the node
                // calling this function
                if(neighborhoodI == owner.getID()) {
                    String[] neighborIDs = nonComment.split(" ");

                    for(int i = 0; i < neighborIDs.length; i ++) {
                        int ithID = Integer.parseInt(neighborIDs[i]);
                        NodeID neighborID = new NodeID(ithID);

                        if(!allNodes.containsKey(neighborID)) throw new IndexOutOfBoundsException();
                        Neighbor neighbor = allNodes.get(neighborID);

                        neighbors.put(neighborID, neighbor);
                    }
                    break;
                }
                neighborhoodI ++;
            }
        }
    }

    public HashMap<NodeID, Neighbor> getNeighbors() {
        return neighbors;
    }

    public NodeID getOwner() {
        return owner;
    }

    public Neighbor getOwnerDetails() {
        return allNodes.get(owner);
    }
}
