package digraph;

import java.util.ArrayList;

public class Edge
{
	//using the ID? look at DiGraph addEdge
    public int sourceNode;
    public int targetNode;
    
    //LC
    private int weight;
    
    public Edge(int from, int to)
    {
        sourceNode = from;
        targetNode = to;
        
        //LC
        weight = 1;
    }
    
    public Edge(int from, int to, int theWeight)
    {
        sourceNode = from;
        targetNode = to;
        
        //LC
        weight = theWeight;
    }
    
    //LC
    public int getWeight()
    {
    	return weight;
    }

    public boolean equals(Edge thatEdge)
    {
        if(this.sourceNode == thatEdge.sourceNode && this.targetNode == thatEdge.targetNode) 
        	return true;
        
        return false;
    }
    
    @Override
    public String toString()
    {
        String edgeString = "";
        
        edgeString += "Edge (";
        edgeString += "From: " + sourceNode + ", ";
        edgeString += "To: " + targetNode + ")";
        
        return edgeString;
    }
}
