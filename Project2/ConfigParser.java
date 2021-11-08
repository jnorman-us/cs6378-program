//package node;

//import node.Neighbor;
//import node.NodeID;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class ConfigParser {
    private int owner;
    private HashMap<Integer, Neighbor> neighbors;
    private HashMap<Integer, Neighbor> allNodes;

    public ConfigParser(
            NodeID owner,
            Node ownerNode,
            String fileName
    ) throws FileNotFoundException, IndexOutOfBoundsException, IllegalArgumentException {
        Scanner file = new Scanner(new File(fileName));
        this.owner = owner.getID();
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

                if(this.owner >= nodeCount) throw new IndexOutOfBoundsException();
            }
            // have found number of nodes, now parsing the ith node's details
            else if(nodeCount >= 0 && currentNodeDetailsI < nodeCount) {
                String[] iNodeDetails = nonComment.split(" ");

                int id = Integer.parseInt(iNodeDetails[0]);
                String hostname = iNodeDetails[1];
                int port = Integer.parseInt(iNodeDetails[2]);

                if(id != currentNodeDetailsI) throw new IllegalArgumentException();

                Neighbor ithNeighborNode = new Neighbor(id, hostname, port, ownerNode);
                allNodes.put(id, ithNeighborNode);
                currentNodeDetailsI ++;
            }
            // all node details parsed, now find the neighbor listing for
            else if(currentNodeDetailsI == nodeCount) {
                // check if current line contains the neighbors for the node
                // calling this function
                if(neighborhoodI == this.owner) {
                    String[] neighborIDs = nonComment.split(" ");

                    for(int i = 0; i < neighborIDs.length; i ++) {
                        int ithID = Integer.parseInt(neighborIDs[i]);

                        if(!allNodes.containsKey(ithID)) throw new IndexOutOfBoundsException();
                        Neighbor neighbor = allNodes.get(ithID);

                        neighbors.put(ithID, neighbor);
                    }
                    break;
                }
                neighborhoodI ++;
            }
        }
    }

    public HashMap<Integer, Neighbor> getNeighbors() {
        return neighbors;
    }

    public int getOwner() {
        return owner;
    }

    public Neighbor getOwnerDetails() {
        return allNodes.get(owner);
    }
}
