package test;

import node.Listener;
import node.Message;
import node.Node;
import node.NodeID;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Application implements Listener
{
    int n;
    NodeID[] exp_neighbors;

    String[] getNextUsefulLine(BufferedReader br)
    {
        try
        {
            String line = br.readLine();
            while (true)
            {
                if(line == null)
                {
                    System.out.println("config error");
                    return null;
                }
                line = line.split("#")[0];
                String[] params = line.trim().split("\\s+");
                if(params.length != 0)
                {
                    return params;
                }
                br.readLine();
            }
        }
        catch(Exception e)
        {
            System.out.println("config error");
            //throw e;
        }
        return null;
    }

    void readFile(String configFile)
    {
        FileReader fr;
        BufferedReader br;
        try
        {
            fr = new FileReader(configFile);
            br = new BufferedReader(fr);
        }
        catch(Exception e)
        {
            System.out.println("config error");
            return;
            //throw e;
        }

        String[] params;
        try
        {
            params = getNextUsefulLine(br);
            n = Integer.parseInt(params[0]);
            for(int i = 0; i < n; i++)
            {
                params = getNextUsefulLine(br);
            }
            for(int i = 0; i < n; i++)
            {
                params = getNextUsefulLine(br);
                if(i == myID.getID())
                {
                    exp_neighbors = new NodeID[params.length];
                    for(int j = 0; j < params.length; j++)
                    {
                        exp_neighbors[j] = new NodeID(Integer.parseInt(params[j]));
                    }
                }
            }
        }
        catch(Exception e)
        {
            //throw e;
        }
    }

    boolean test1_result;
    //Check if getNeighbors works properly
    synchronized void test1()
    {
        boolean[] neighborMatch = new boolean[n];
        int i;
        int idx;
        for(i = 0; i < n; i++)
        {
            neighborMatch[i] = true;
        }
        for(NodeID id : exp_neighbors)
        {
            idx = id.getID();
            neighborMatch[idx] ^= true;
        }
        for(NodeID id : myNode.getNeighbors())
        {
            idx = id.getID();
            neighborMatch[idx] ^= true;
        }
        for(i = 0; i < n; i++)
        {
            if(!neighborMatch[i])
            {
                test1_result = false;
                return;
            }
        }
        test1_result = true;
    }

    boolean test2_result = false;
    synchronized void test2()
    {
        try
        {
            if(myID.getID() == 0)
            {
                Payload p = new Payload(1);
                Message msg = new Message(myID, p.toBytes());
                try{myNode.send(msg, new NodeID(1));}catch(Exception e){test2_result=false;}
                int i = 0;
                while(test2_result == false)
                {
                    wait(2000);
                    i++;
                    if(i > 5) break;
                }
            }
            else
            {
                test2_result = true;
            }
        }
        catch(InterruptedException ie)
        {
            System.out.println("InterruptedException at Node " + myID.getID() + " during test 1");
        }
    }

    boolean test3_result = false;
    int rec_count = 0;
    synchronized void test3()
    {
        try
        {
            if(myID.getID() == 0)
            {
                Payload p = new Payload(2);
                Message msg = new Message(myID, p.toBytes());
                try{myNode.sendToAll(msg);}catch(Exception e){test3_result=false;}
                int i = 0;
                while(test3_result == false)
                {
                    wait(20);
                    i++;
                    if(i > 10) break;
                }
            }
            else
            {
                test3_result = true;
            }
        }
        catch(InterruptedException ie)
        {
            System.out.println("InterruptedException at Node " + myID.getID() + " during test 1");
        }
    }

    synchronized void test()
    {
        System.out.println("Node " + myID.getID() + " starting tests");
        test1();
        System.out.println("Node " + myID.getID() + " completed test 1");
        test2();
        System.out.println("Node " + myID.getID() + " completed test 2");
        test3();
        System.out.println("Node " + myID.getID() + " completed test 3");
        if(myID.getID() == 0)
        {
            try{myNode.tearDown();}catch(Exception e){}
        }
    }




    Node myNode;
    NodeID myID;

    NodeID[] neighbors;
    boolean[] brokenNeighbors;

    boolean detectingRing;
    boolean isRing;
    NodeID pred;

    boolean terminating;

    public synchronized void receive(Message message)
    {
        Payload p = Payload.getPayload(message.data);
        if(p.messageType == 1)
        {
            if(myID.getID() == 1)
            {
                Payload newp = new Payload(-1);
                Message msg = new Message(myID, newp.toBytes());
                try{myNode.send(msg, message.source);}catch(Exception e){test2_result=false;}
            }
            else
            {
                test2_result = false;
            }
        }
        else if(p.messageType == -1)
        {
            if(myID.getID() == 0)
            {
                test2_result = true;
                notifyAll();
            }
            else
            {
                test2_result = false;
            }
        }
        else if(p.messageType == 2)
        {
            Payload newp = new Payload(-2);
            Message msg = new Message(myID, newp.toBytes());
            try{myNode.send(msg, message.source);}catch(Exception e){test3_result=false;}
        }
        else if(p.messageType == -2)
        {
            if(myID.getID() == 0)
            {
                rec_count++;
                if(rec_count == exp_neighbors.length)
                {
                    test3_result = true;
                    notifyAll();
                }
            }
            else
            {
                test3_result = false;
            }
        }
    }

    public synchronized void broken(NodeID neighbor)
    {
        for(int i = 0; i < neighbors.length; i++)
        {
            if(neighbor.getID() == neighbors[i].getID())
            {
                brokenNeighbors[i] = true;
                notifyAll();
                if(!terminating)
                {
                    terminating = true;
                    try{myNode.tearDown();}catch(Exception e){}
                }
                return;
            }
        }
    }

    String configFile;

    public Application(NodeID identifier, String configFile)
    {
        myID = identifier;
        this.configFile = configFile;
    }

    public synchronized void run()
    {
        readFile(configFile);
        try{myNode = new Node(myID, configFile, this);}catch(Exception e){throw e;}
        neighbors = myNode.getNeighbors();
        brokenNeighbors = new boolean[neighbors.length];
        for(int i = 0; i < neighbors.length; i++)
        {
            brokenNeighbors[i] = false;
        }
        test();
        int j = 0;
        for(int i = 0; i < neighbors.length; i++)
        {
            while(!brokenNeighbors[i])
            {
                try
                {
                    wait(50);
                    j++;
                    if(j > 8) break;
                }
                catch(InterruptedException ie)
                {
                }
            }
        }
        if(test1_result == false)
        {
            try{
                BufferedWriter writer = new BufferedWriter(new FileWriter("FAIL_test1_Node" + myID.getID()));
                writer.write("FAIL");
                writer.close();
            }
            catch(Exception e)
            {
                System.out.println("IOException");
            }
        }
        if(test2_result == false)
        {
            try{
                BufferedWriter writer = new BufferedWriter(new FileWriter("FAIL_test2_Node" + myID.getID()));
                writer.write("FAIL");
                writer.close();
            }
            catch(Exception e)
            {
                System.out.println("IOException");
            }
        }
        if(test3_result == false)
        {
            try{
                BufferedWriter writer = new BufferedWriter(new FileWriter("FAIL_test3_Node" + myID.getID()));
                writer.write("FAIL");
                writer.close();
            }
            catch(Exception e)
            {
                System.out.println("IOException");
            }
        }
        if(test1_result && test2_result && test3_result && myID.getID() == 0)
        {
            System.out.println("Passed");
            try{
                BufferedWriter writer = new BufferedWriter(new FileWriter("PASS_Node" + myID.getID()));
                writer.write("PASS");
                writer.close();
            }
            catch(Exception e)
            {
                System.out.println("IOException");
            }
        }
    }
}
