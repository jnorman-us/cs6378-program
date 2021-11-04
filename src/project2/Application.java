package project2;

import node.Listener;
import node.Message;
import node.NodeID;
import node.Node;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

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



    



    public Application(NodeID identifier, String configFile) {
       
        myID = identifier;
        this.configFile = configFile;
    }
    
    
    
    //synchronized receive called when Node receives a message
    public synchronized void receive(Message message) {

        Payload p = Payload.getPayload(message.data);

        if (p.round == round){

            //process payload
            for (int j = 0; j < p.nodeList.length; j++){
                thisHop.add(p.nodeList[j]);
            }

            //TODO YOU ARE HERE!!


            //count reply
            replyCount++;
        }

        else {

            //queue payload
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

            //send next round of messages
            Payload newp = new Payload(round, neighbors[round-1]);
            Message newm = new Message(myID, newp.toBytes());
            myNode.sendToAll(newm);

            //process queue and increment reply count for new round if needed


            //figure out if all nodes have been found.
            //if so, set flag
            //return to run


        }
    }

    
    //
    public synchronized void broken(NodeID neighbor) {

        //if neighbor is broken, decrement expected replies
    }

    //synchronized, control tranfser on wait or return
    public synchronized void run() {
        //initialize round to 1
        round = 1;

        //TODO get total number of nodes from config file
        totalNodes = 10;

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
        oneHop = myNode.getNeighbors().length;

        for (int i = 0; i < oneHop; i++){
            nodesFound.add(neighbors[1][i]);

        }

        //ready to start receiving
        round = 2;

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

    
}
