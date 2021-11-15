package project2;

import node.Listener;
import node.Message;
import node.Node;
import node.NodeID;
import project2.Payload;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

class NodeIDComparator implements Comparator<NodeID> {
    public int compare(NodeID n1, NodeID n2) {
        if(n1.getID() == n2.getID()) {
            return 0;
        }
        else if (n1.getID() > n2.getID()) {
            return 1;
        }
        else {
            return -1;
        }
    }
}

public class Application implements Listener {
    int round;
    int oneHop;
    int replyCount;
    int totalNodes;

    String configFile;

    Node myNode;

    NodeID myID;

    NodeID[][] neighbors;

    PriorityBlockingQueue<Payload> queuedMsgs = new PriorityBlockingQueue<>(20, (a, b) -> a.round - b.round);

    ArrayList<Integer> noDupes = new ArrayList<>();
    ArrayList<NodeID> thisHop = new ArrayList<>();
    ArrayList<Integer> nodesFound = new ArrayList<>();

    boolean allNodesFound;
    boolean processingQueue;

    public Application(NodeID identifier, String configFile) {
        myID = identifier;
        this.configFile = configFile;
    }

    //synchronized receive called when Node receives a message
    public synchronized void receive(Message message) {
        Payload p = (Payload) message;
        if (p.round == round) {

            //process payload
            if (p.kHopNeighbors != null && p.kHopNeighbors.length != 0) { // changed because of possible access of length while null
                for (int j = 0; j < p.kHopNeighbors.length; j++) {
                    thisHop.add(p.kHopNeighbors[j]);
                }
            }
            //count reply
            replyCount++;
        }
        else {
            //queue payload
            queuedMsgs.add(p);
        }

        //check queue for pending messages for this current round
        processingQueue = true;

        //process queue and increment reply count for new round if needed
        while (processingQueue) {
            //process queue until all round messages done
            processQueue();
        }

        if (replyCount == oneHop) {

            //all messages recevied and processed for this round

            //remove dups received this round
            thisHop.removeIf(n -> n == null);
            for (NodeID thisNode : thisHop){
                if(!noDupes.contains(thisNode.getID())){
                    noDupes.add(thisNode.getID());
                }
            }
            thisHop.clear();

            for (int num : noDupes){
                thisHop.add(new NodeID(num));
            }
            noDupes.clear();

            //check thisHop v. nodesFound to remove previous found duplicates.
            thisHop.removeIf(n -> (nodesFound.contains(n.getID())));

            //sort in nodeID order
            Collections.sort(thisHop, new NodeIDComparator());

            //add thisHop to neighbors[round] and nodesFound
            for(NodeID thisHopNode : thisHop) {
                nodesFound.add(thisHopNode.getID());
            }
            thisHop.toArray(neighbors[round]);

            //clear thisHop
            thisHop.clear();

            //reset replyCount
            replyCount = 0;

            //update round
            round++;

            //if round = total nodes, we're done --> break/return to print
            if (round == totalNodes){

                allNodesFound = true;
                notifyAll();
                //return
            }

            else{
                //check queue for pending messages for this new round
                processingQueue = true;

                //process queue and increment reply count for new round if needed
                while (processingQueue){

                    //process queue until all round messages done
                    processQueue();
                }

                Payload newm = new Payload(round, myID, neighbors[round-1]);
                myNode.sendToAll(newm);
            }
        }

    }

    public synchronized void processQueue(){
        if(queuedMsgs.isEmpty()){
            processingQueue = false;
        }

        else{
            Payload nextPayload = queuedMsgs.peek();

            if (nextPayload.round == round){
                Payload incomingPayload = queuedMsgs.poll();

                //process payload, if not null/empty
                if (!(incomingPayload.kHopNeighbors == null || incomingPayload.kHopNeighbors.length == 0)){
                    for (int k = 0; k < incomingPayload.kHopNeighbors.length; k++){
                        thisHop.add(incomingPayload.kHopNeighbors[k]);
                    }
                }
                replyCount++;
            }

            else {
                //queued messages aren't for the current round.
                processingQueue = false;
            }
        }
    }


    public synchronized void broken(NodeID neighbor) {

        //do nothing
        //neighbors should only terminate after all msgs sent
    }

    //returns totalNodes from configFile
    public int getTotalNodes() {
        int numNodes = -1;
        try {
            Scanner scan = new Scanner(new File(configFile));
            String line;

            while(scan.hasNextLine()) {
                line = scan.nextLine();
                //have not updated numNodes
                if(numNodes == -1) {
                    //get rid of line comments #
                    if(!line.startsWith("#")){
                        //get rid of everything after #
                        line = line.split("#")[0];
                        numNodes = Integer.parseInt(line);
                    }
                }
            }

            scan.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        return numNodes;
    }

    //synchronized, control tranfser on wait or return
    public synchronized void run() {
        //initialize round to 2
        round = 2;
        allNodesFound = false;
        processingQueue = false;

        //get total number of nodes from config file
        totalNodes = getTotalNodes();

        //initialize neighbors array
        neighbors = new NodeID[totalNodes][totalNodes - 1];

        //Construct my node
        myNode = new Node(myID, configFile, this);
        NodeID[] me = new NodeID[1];

        //add my node as root in neighbors
        //count my node as found
        me[0] = myID;
        neighbors[0] = me;
        nodesFound.add(myID.getID());

        //get count of onehop neighbors
        //add onehop neighbors to array
        //count onehope neighbors as found
        neighbors[1] = myNode.getNeighbors();
        oneHop = neighbors[1].length;

        for (int i = 0; i < oneHop; i++){
            nodesFound.add(neighbors[1][i].getID());

        }


        //send initial round of msgs
        Payload first = new Payload(round, myID, neighbors[1]);
        myNode.sendToAll(first);

        // receive k-1 rounds of msgs
        while(!allNodesFound){

            try{

                wait();
            }

            catch(InterruptedException ie){

            }

        }


        try{
            String filename = myID.getID() + "-" + configFile;
            printNodes(neighbors, filename);
        } catch (IOException ie){
            System.out.print(ie);
        }

        //teardown node once all msgs complete
        myNode.tearDown();

        System.out.println("Node " + myID.getID() + " complete.");

    }

    public void printNodes(NodeID[][] nodes, String outputFile) throws IOException {
        File file = new File(outputFile);
        file.createNewFile();

        FileWriter fout = new FileWriter(file, false);

        for(int k = 1; k < nodes.length; k ++) { // skipping round 0 (self)
            fout.write(k + ":");
            for(int i = 0; i < nodes[k].length; i ++) {
                if(nodes[k][i] != null) {
                    fout.write(" " + nodes[k][i].getID());
                }
            }
            fout.write("\n");
        }
        fout.close();
    }
}