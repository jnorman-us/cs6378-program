package project2;

import node.Listener;
import node.Message;
import node.NodeID;
import node.Node;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Application implements Listener {
    int round;
    int oneHop;
    int replyCount;
    int totalNodes;

    String configFile;

    Node myNode;
    
    NodeID myID;
    
    NodeID[][] neighbors;
    
    PriorityBlockingQueue<Payload> queuedMsgs = new PriorityBlockingQueue<>();

    ArrayList<NodeID> thisHop = new ArrayList<>();
    ArrayList<NodeID> nodesFound = new ArrayList<>();

    boolean allNodesFound;
    boolean processQueue;

    
    public Application(NodeID identifier, String configFile) {

       
        myID = identifier;
        this.configFile = configFile;
    }
    
    
    //synchronized receive called when Node receives a message
    public synchronized void receive(Message message) {

        Payload p = (Payload) message;

        if (p.round == round){

            //process payload
            if (!(p.kHopNeighbors == null || p.kHopNeighbors.length == 0)){
                for (int j = 0; j < p.kHopNeighbors.length; j++){
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

        if (replyCount == oneHop){

            //all messages recevied and processed for this round

            //check thisHop v. nodesFound to remove duplicates.
            thisHop.removeIf(n -> (nodesFound.contains(n)));

            //add thisHop to neighbors[round]
            thisHop.toArray(neighbors[round]);
            
            //clear thisHop
            thisHop.clear();

            //reset replyCount
            replyCount = 0;
            
            //update round
            round++;

            //check queue
            processQueue = true;
           
            //process queue and increment reply count for new round if needed
            while (processQueue){
                
                if(!queuedMsgs.isEmpty()){

                    processQueue = false;
                }

                else{
                    Payload nextPayload = queuedMsgs.peek();
                    if (nextPayload.round == round){
                        Payload incomingPayload = queuedMsgs.poll();
                        
                        //process payload, if not null/empty
                        if (!(p.kHopNeighbors == null || p.kHopNeighbors.length == 0)){
                            for (int k = 0; k < incomingPayload.kHopNeighbors.length; k++){
                                thisHop.add(incomingPayload.kHopNeighbors[k]);
                            }
                        }
                        replyCount++;
                    }

                    else{
                        //queued messages aren't for the current round.
                        processQueue = false;
                        
                    }
                }
            }

            //figure out if all nodes have been found.
            //if so, set flag
            //return to run
            Payload newm = new Payload(round, myID, neighbors[round-1]);
            myNode.sendToAll(newm);
        }
        
    }

    synchronized void processQueue(){}

    
    //
    public synchronized void broken(NodeID neighbor) {

        //if neighbor is broken, decrement expected replies
        //need to see if queue is empty first, or the count might get off
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
        //initialize round to 1
        round = 1;

        //TODO get total number of nodes from config file
        totalNodes = getTotalNodes();

        //initialize neighbors array
        neighbors = new NodeID[totalNodes][];

        //Construct my node
        myNode = new Node(myID, configFile, this);
        NodeID[] me = new NodeID[1];
        
        //add my node as root in neighbors
        //count my node as found
        me[0] = myID;
        neighbors[0] = me;
        nodesFound.add(myID);

        //get count of onehop neighbors
        //add onehop neighbors to array
        //count onehope neighbors as found
        neighbors[1] = myNode.getNeighbors();
        oneHop = neighbors[1].length;

        for (int i = 0; i < oneHop; i++){
            nodesFound.add(neighbors[1][i]);

        }

        //ready to start receiving
        round = 2;
        allNodesFound = false;
        processQueue = false;

        //TODO figure out transfer of control thing
        while(!allNodesFound){

            try{

                wait();
            }

            catch(InterruptedException ie){
                
            }



        }
        
        
        
        //TODO log khops to file



        
        

    }

    public void printNodes(NodeID[][] nodes, String outputFile) throws IOException {
        File file = new File(outputFile);
        file.createNewFile();

        FileWriter fout = new FileWriter(file, false);

        for(int k = 0; k < nodes.length; k ++) {
            fout.write((k + 1) + ":");
            for(int i = 0; i < nodes[k].length; i ++) {
                fout.write(" " + nodes[k][i].getID());
            }
            fout.write("\n");
        }
        fout.close();
    }
   

}
