package project2;

import node.Message;
import node.NodeID;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

class Payload implements java.io.Serializable {
   
    int round;
    NodeID[] nodeList;

    public Payload(int rnd, NodeID[] kbors) {
        round = rnd;
        nodeList = kbors;
    }

    public static Payload getPayload(byte[] payloadBytes){
         
        
        Payload p = null;
                
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(payloadBytes);
            ObjectInputStream in = new ObjectInputStream(bis);
            p = (Payload) in.readObject();
            in.close();
            
                        
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return p;
      
        
    }

   
    public byte[] toBytes()
	{
		//Output streams help with serialization
		
		byte[] result = null;
		try 
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutputStream oos = new ObjectOutputStream(bos);   
			oos.writeObject(this);
			oos.flush();
            result = bos.toByteArray();
            bos.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return result;
	}
}
