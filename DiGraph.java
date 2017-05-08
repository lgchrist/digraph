package digraph;

import java.util.ArrayList;
import java.util.Hashtable;
import utilities.DepthFirstSearch;
import hypergraph.*;
import java.util.Queue;
import java.util.LinkedList;

public class DiGraph<T>
{
    private ArrayList<Node<T>> nodes;
    protected ArrayList<Integer> nodesInt;
    
    //key(integer), value (arrayList)
    protected Hashtable<Integer, ArrayList<Integer>> edgeMap;
    protected Hashtable<Integer, ArrayList<Integer>> transposeEdgeMap;
    private ArrayList<Edge> edges;
    private int numEdges;
    
    public DiGraph()
    {
        nodes = new ArrayList<Node<T>>();
        nodesInt = new ArrayList<Integer>();
        edgeMap = new Hashtable<Integer, ArrayList<Integer>>();
        transposeEdgeMap = new Hashtable<Integer, ArrayList<Integer>>();
        edges = new ArrayList<Edge>();
        numEdges = 0;
    }
    
    public DiGraph(Hypergraph HG)
    {
        nodes = new ArrayList<Node<T>>();
        nodesInt = new ArrayList<Integer>();
        
        //tried for each loop
        for(int currNode = 0; currNode < HG.vertices.size(); currNode++)
        {
        	//Node(T theData, int theId), the id's start at 0!
            Node newNode = new Node(HG.getNode(currNode).data, currNode);
            nodes.add(newNode);
            nodesInt.add(currNode);
        }
        
        edgeMap = new Hashtable<Integer, ArrayList<Integer>>();
        transposeEdgeMap = new Hashtable<Integer, ArrayList<Integer>>();
        edges = new ArrayList<Edge>();
        numEdges = 0;
        
        for(int currNodeIndex = 0; currNodeIndex < HG.vertices.size(); currNodeIndex++)
        {
            Hypernode currNode = HG.getNode(currNodeIndex);
            for(int currEdgeIndex = 0; currEdgeIndex < currNode.inEdges.size(); currEdgeIndex++)
            {
                Hyperedge currEdge = currNode.getInEdge(currEdgeIndex);
                AddHyperEdge(currEdge.sourceNodes, currEdge.targetNode);
            }
            for(int currEdgeIndex = 0; currEdgeIndex < currNode.outEdges.size(); currEdgeIndex++)
            {
                Hyperedge currEdge = currNode.getOutEdge(currEdgeIndex);
                AddHyperEdge(currEdge.sourceNodes, currEdge.targetNode);
            }
        }
    }
    
    private boolean isAcyclic()
    {
        DepthFirstSearch DFS = new DepthFirstSearch(this);
        
        return DFS.getBackEdges().isEmpty();
    }
    
    public ArrayList<Node<T>> topologicalSort()
    {
        if(isAcyclic() == true)
        {
            DepthFirstSearch DFS = new DepthFirstSearch(this);
            return DFS.getTopologicalList();
        }
        return null;
    }
    
    public ArrayList<Linearization<T>> allTopologicalSort()
    {
        if(isAcyclic() == true)
        {
            DepthFirstSearch DFS = new DepthFirstSearch(this);
            return DFS.getAllTopologicalSorts();
        }
        return null;
    }
    
    public void addNode(T data)
    {
        nodes.add(new Node<T>(data, nodes.size()));
    }
    
    // Adds an edge to a map of edges
    private void AddEdge(Hashtable<Integer, ArrayList<Integer>> givenEdges, int from, int to)
    {
        // This order needed because we want the goal node of the problem first
        if(!nodesInt.contains(to)) nodesInt.add(to);
        if(!nodesInt.contains(from)) nodesInt.add(from);

        ArrayList<Integer> fromDependencies = new ArrayList<Integer>();
        if(givenEdges.containsKey(from))
        {
            givenEdges.get(from).add(to);
        }
        else
        {
            fromDependencies.add(to);
            givenEdges.put(from, fromDependencies);
        }
    }

    //
    // Adds a many-to-one hyperedge to the graph by adding all the individual edges
    //
    public void AddHyperEdge(ArrayList<Integer> fromList, int to)
    {
        for(Integer from : fromList)
        {
            this.addEdge(from, to);
        }
    }
    
    public void addEdge(int from, int to)
    {
        Edge newEdge = new Edge(from, to);
        
        if(this.hasEdge(newEdge) == false)
        {
            nodes.get(from).addEdge(newEdge);
            nodes.get(to).addEdge(newEdge);
            edges.add(newEdge);
            
            AddEdge(edgeMap, from, to);
            AddEdge(transposeEdgeMap, to, from);

            numEdges++;
        }
    }
    
    public ArrayList<Node<T>> getVertices()
    {
        return nodes;
    }
    
    public ArrayList<Edge> getEdges()
    {
        return edges;
    }
    
    //checks and returns if each vertex is incident to the edge
    public boolean hasEdge(Edge e)
    {
        for(Edge currEdge: edges)
        {
            if(e.equals(currEdge)) return true;
        }
        return false;
    }
    
    //returns the node with a specific id
    public Node<T> getNode(int id)
    {
        return nodes.get(id);
    }
    
    
    //LC:
    
//    private ArrayList<Node<T>> nodes;
//    protected ArrayList<Integer> nodesInt;
//    
//    //key(integer), value (arrayList)
//    protected Hashtable<Integer, ArrayList<Integer>> edgeMap;
//    protected Hashtable<Integer, ArrayList<Integer>> transposeEdgeMap;
//    private ArrayList<Edge> edges;
//    private int numEdges;
    
    //return target nodes 
    public ArrayList<Node<T>> getTargetNodes(int source)
    {
    	ArrayList<Node<T>> targetNodes = new ArrayList<Node<T>>();
    	
    	//get indexes for the target nodes use 
    	//nodesInt -> nodes
    	//will keep looping the size of the ArrayList from edgeMap
    	for(int i = 0; i < edgeMap.get(source).size(); i++)
    	{
    		//taking index from 'edgeMap' to get node from 'nodes'
    		//edgeMap.get(source) == will return ArrayList<Integer>>
    		//edgeMap.get(source).get(i) == will return the index of the ArrayList
    		//nodes.get(edgeMap.get(source).get(i)) == will return the node 
    		//determined from the index from the edgeMap
    		targetNodes.add(nodes.get(edgeMap.get(source).get(i)));
    	}
    	
    	return targetNodes;
    }//end LC
   
    
    public int getNumNode()
    {
    	return nodes.size();
    }
    
    // The depth of the graph is defined as being the length of the maximal path to the leaf nodes
    // We assume that this graph is a DAG.
    //
    // We use a DFS technique to determine maximality
    //
    // Since we have an implied 1-1 map between vertices and indices, we work on indices
    //
    public int GetLength()
    {
        // passing index 0, depth 1
        return GetLengthHelper(0, 1);
    }
    private int GetLengthHelper(int currentNode, int currentDepth)
    {
        int maxDepth = -1;

        // Get the edges from this node to traverse
        ArrayList<Integer> forwardEdges = new ArrayList<Integer>();

        // Get the transpose edges from this node (as we are traversing backward from the goal)
        // This node is a leaf if no edges: return the known depth
        if(!edgeMap.containsKey(currentNode)) return currentDepth;
        else forwardEdges = edgeMap.get(currentNode);

        // Traverse the edges tracking the max depth
        for(int edge : forwardEdges)
        {
            int tempDepth = GetLengthHelper(edge, currentDepth + 1);
            if (maxDepth < tempDepth) maxDepth = tempDepth;
        }

        return maxDepth;
    }
    
    // General graph traversal assuming a DAG; we start at the goal node and walk the transpose edges
    //
    // Since this is a DAG, width is defined as the spread of the graph (like a tree) 
    // Use a BFS technique where we continually update the levelWidth variable to ensure we know where the next level starts
    //
    // Since we have an implied 1-1 map between vertices and indices, we work on indices
    //
//    public int GetWidth() throws Exception
//    {
//        Queue<Integer> worklist = new LinkedList<Integer>();
//
//        // Add the 'goal' node as a catalyst
//        worklist.addAll(getSink());
//
//        // width for this level
//        int currentLevelWidth = 1;
//
//        // max width for the entire graph
//        int maxLevelWidth = 1;
//
//        // For verification purposes, we track the total number of nodes visited
//        int sumOfAllWidths = 0;
//
//        //
//        // Traverse the entire graph
//        //
//        while (!worklist.isEmpty())
//        { 
//            // Traverse an entire level
//            int currentLevelAccumulator = 0;
//            for (int ell = 0; ell < currentLevelWidth; ell++)
//            {
//                // Get the next node
//                int currentNode = worklist.poll();
//
//                // Get the edges from this node to traverse
//                ArrayList<Integer> backwardEdges = new ArrayList<Integer>();
//                if (transposeEdgeMap.containsKey(currentNode))
//                {
//                    backwardEdges = transposeEdgeMap.get(currentNode);
//                    // Add all targets to the worklist
//                    for(int edgeIndex : backwardEdges)
//                    {
//                        if(!worklist.contains(edgeIndex)) worklist.add(edgeIndex);
//                    }
//                }
//                currentLevelAccumulator += backwardEdges == null ? 0 : backwardEdges.size();
//            }
//
//            // Completed a level; check width values, including if we have a new maxWidth
//            if (currentLevelWidth > maxLevelWidth) maxLevelWidth = currentLevelWidth;
//
//            sumOfAllWidths += currentLevelWidth;
//
//            // Next level's number of nodes
//            currentLevelWidth = currentLevelAccumulator;
//        }
//
//        if (sumOfAllWidths < nodesInt.size())
//        {
//            throw new Exception("Error in width determination: Did not traverse all nodes!");
//        }
//
//        return maxLevelWidth;
//    }
    
    //
    //MY GETWIDTH
    //
    public int GetWidth() throws Exception
    {
        Queue<Integer> worklist = new LinkedList<Integer>();

        // Add the 'goal' node as a catalyst
        worklist.addAll(getSink());
        
        ArrayList<Integer> nextLevel = new ArrayList<Integer>();

        // width for this level
        int currentLevelWidth = worklist.size();

        // max width for the entire graph
        int maxLevelWidth = worklist.size();

        // For verification purposes, we track the total number of nodes visited
        int sumOfAllWidths = 0;
        
        ArrayList<Integer> worked = new ArrayList<Integer>();

        //
        // Traverse the entire graph
        //
        while (!worklist.isEmpty())
        { 
            // Traverse an entire level
            for (int ell = 0; ell < currentLevelWidth; ell++)
            {
                // Get the next node
                int currentNode = worklist.poll();
                
                worked.add(currentNode);

                // Get the edges from this node to traverse
                ArrayList<Integer> backwardEdges = new ArrayList<Integer>();
                if (transposeEdgeMap.containsKey(currentNode))
                {
                    backwardEdges = transposeEdgeMap.get(currentNode);
                    // Add all targets to the worklist
                    for(int edgeIndex : backwardEdges)
                    {
                        if(!nextLevel.contains(edgeIndex) && !worked.contains(edgeIndex)) nextLevel.add(edgeIndex);
                    }
                }
            }
            
            // Completed a level; check width values, including if we have a new maxWidth
            if (currentLevelWidth > maxLevelWidth) maxLevelWidth = currentLevelWidth;

            sumOfAllWidths += currentLevelWidth;

            // Next level's number of nodes
            currentLevelWidth = nextLevel.size();
            
            worklist.addAll(nextLevel);
            nextLevel.clear();
        }

        if (sumOfAllWidths < nodesInt.size())
        {
            throw new Exception("Error in width determination: Did not traverse all nodes!");
        }

        return maxLevelWidth;
    }
    
    public ArrayList<Integer> getSource()
    {
        ArrayList<Integer> sources = new ArrayList<Integer>();
        
        for(Node currNode : nodes)
        {
            if(currNode.inEdges.isEmpty()) sources.add(currNode.getId());
        }
        
        return sources;
    }
    
    public ArrayList<Integer> getSink()
    {
        ArrayList<Integer> sinks = new ArrayList<Integer>();
        
        for(Node currNode : nodes)
        {
            if(currNode.outEdges.isEmpty()) sinks.add(currNode.getId());
        }
        
        return sinks;
    }
    
    @Override
    public String toString()
    {
        String graphS = "";
        
        for(Node<T> currNode: nodes)
        {
            if(nodes.indexOf(currNode) != 0) graphS += ", [Vertex " + nodes.indexOf(currNode) + "]: ";
            else graphS += "[Vertex " + nodes.indexOf(currNode) + "]: ";
            graphS += "(data: " + currNode.data + " / ";
            graphS += "out edges: ";
            if(currNode.outEdges.isEmpty()) graphS += "none ";
            for(Edge currEdge: currNode.outEdges)
            {
                int lastIndex = currNode.outEdges.size() - 1;
                if((currNode.outEdges.indexOf(currEdge)) != (lastIndex))
                {
                    graphS += currEdge.toString() + ", ";
                }
                else graphS += currEdge.toString();
            }
            graphS += " / in edges: ";
            if(currNode.inEdges.isEmpty()) graphS += "none ";
            for(Edge currEdge: currNode.inEdges)
            {
                graphS += currEdge.toString();
            }
            graphS += ")\n";
        }
        
        return graphS;
    }
}
