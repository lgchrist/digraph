package digraph;

import java.util.ArrayList;
import java.util.Collection;

public class Node<T> implements Comparable<Node>
{
	public T data;
	private int id;

	//LC
	//three pieces of info to update for each node
	private boolean known;
	private int lengthOfPath;
	private Node predecessor;

	//nodes which are a target of edges in which this node is the source
	public ArrayList<Integer> descendants; 

	//edges in which this node is the source
	public ArrayList<Edge> outEdges;

	//edges in which this node is the target
	public ArrayList<Edge> inEdges;

	//constructor
	public Node(T theData, int theId)
	{
		data = theData; //is this the weight of the edge from one node to another?
		id = theId;
		outEdges = new ArrayList<Edge>();
		inEdges = new ArrayList<Edge>();
		descendants = new ArrayList<Integer>();  
		known = false;
		lengthOfPath = Integer.MAX_VALUE;
		predecessor = null;
	}

	public void addEdge(Edge e)
	{
		if(!hasEdge(e))
		{
			if(e.sourceNode == id) outEdges.add(e);
			if(e.targetNode == id) inEdges.add(e);
			if(id != e.targetNode) this.descendants.add(e.targetNode);
		}
	}

	//LC
	public int getWeight(int oneTargetNode)
	{
		int weight; 
		//take the source and get the weight of the edge to the oneTargetNode
		weight = outEdges.get(oneTargetNode).getWeight();

		return weight;
	}

	//LC
	public int getId()
	{
		return id;
	}

	//LC
	public void setPredecessor(Node pred)
	{
		predecessor = pred;
	}


	//how to prioritize nodes??
	public int getLengthOfPath()
	{
		return lengthOfPath;
	}

	//LC
	public void setLengthOfPath(int dist)
	{
		lengthOfPath = dist;
	}
	
	//LC
	public void setKnown(boolean known)
	{
		this.known = known;
	}

	//LC
	@Override
	public int compareTo(Node o)
	{
		//if the 2 distances are the same
		if(this.lengthOfPath == o.lengthOfPath)
		{
			return 0;
		}
		//if this.Node's length of path is greater than the other
		else if(this.lengthOfPath > o.lengthOfPath)
		{
			return 1;
		}
		//if this.Node's distance is lower
		else
			return -1;
	}

	public T getData()
	{
		return data;
	}

	public boolean hasEdge(Edge e)
	{
		for(Edge currEdge: outEdges)
		{
			if(e.equals(currEdge)) return true;
		}

		for(Edge currEdge: inEdges)
		{
			if(e.equals(currEdge)) return true;
		}
		return false;
	}

	public ArrayList getChildren()
	{
		return descendants;
	}

	@Override
	public String toString()
	{
		return data.toString();
	}

}
