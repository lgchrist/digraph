package digraph;
import java.util.ArrayList;
import java.util.*;
import java.util.Stack;
import utilities.*;

//
// Implements a basic directional graph (with no node information)
//
public class DiGraphInt
{
    //
    // To implement Tajan's Strongly Connected Components (and cycles in the graph)
    //
    // Each node v is assigned a unique integer v.index, which numbers the nodes consecutively in the order
    // in which they are discovered. It also maintains a value v.lowlink that represents (roughly speaking)
    // the smallest index of any node known to be reachable from v, including v itself. Therefore v must be
    // left on the stack if v.lowlink < v.index, whereas v must be removed as the root of a strongly connected
    // component if v.lowlink == v.index. The value v.lowlink is computed during the depth-first search from v,
    // as this finds the nodes that are reachable from v.
    //
    //protected class Vertex
    //{
    //    public int node;
    //    public int lowLink;
    //    public int index;

    //    public Vertex(int n)
    //    {
    //        node = n;
    //        lowLink = -1;
    //        index = -1;
    //    }

    //    public Vertex(Vertex v)
    //    {
    //        node = v.node;
    //        lowLink = -1;
    //        index = -1;
    //    }

    //    public override bool Equals(object obj) { return this.node.Equals((obj as Vertex).node); }
    //    public override int GetHashCode() { return base.GetHashCode(); }
    //}

    //
    // The Hashtable is a map: a node to all of its successors
    //
    protected Hashtable<Integer, ArrayList<Integer>> edgeMap;
    protected Hashtable<Integer, ArrayList<Integer>> transposeEdgeMap;
    protected int numEdges;
    protected ArrayList<Integer> vertices;
    protected ArrayList<ArrayList<Integer>> sccs; // Strongly Connected Components

    public DiGraphInt()
    {
        edgeMap = new Hashtable<Integer, ArrayList<Integer>>();
        transposeEdgeMap = new Hashtable<Integer, ArrayList<Integer>>();
        numEdges = 0;
        vertices = new ArrayList<Integer>();
        sccs = new ArrayList<ArrayList<Integer>>();
    }

    //
    // Make a shallow copy of this graph (all vertices and edges)
    //
    public DiGraphInt(DiGraphInt thatGraph)
    {
        edgeMap = new Hashtable<Integer, ArrayList<Integer>>();
        transposeEdgeMap = new Hashtable<Integer, ArrayList<Integer>>();
        numEdges = thatGraph.numEdges;
        vertices = new ArrayList<Integer>(thatGraph.vertices);

        // Copy the integer indices
        for (Integer key : thatGraph.edgeMap.keySet())
        {
            edgeMap.put(key, new ArrayList<Integer>(thatGraph.edgeMap.get(key)));
        }

        //            foreach (KeyValuePair<int, List<int>> pair in thatGraph.edgeMap)
        //            {
        //                edgeMap.Add(pair.Key, new List<int>(pair.Value));
        //            }

        // Copy the integer indices
        for (Integer key : thatGraph.transposeEdgeMap.keySet())
        {
            transposeEdgeMap.put(key, new ArrayList<Integer>(thatGraph.transposeEdgeMap.get(key)));
        }

        sccs = GetStronglyConnectedComponents();
    }

    //
    // Adds a basic edge to the graph
    //
    public void AddEdge(int from, int to)
    {
        AddEdge(edgeMap, from, to);
        AddEdge(transposeEdgeMap, to, from);

        numEdges++;
    }

    // Adds an edge to a map of edges
    private void AddEdge(Hashtable<Integer, ArrayList<Integer>> givenEdges, int from, int to)
    {

        // This order needed because we want the goal node of the problem first
        if (!vertices.contains(to)) vertices.add(to);
        if (!vertices.contains(from)) vertices.add(from);

        ArrayList<Integer> fromDependencies = null;
        if (givenEdges.containsKey(from))
        {
            fromDependencies = givenEdges.get(from);
            Utilities.addUnique(fromDependencies, to);
        }
        else
        {
            ArrayList<Integer> toList = new ArrayList<>();
            toList.add(to);
            givenEdges.put(from, toList);
        }
    }

    //
    // Adds a many-to-one hyperedge to the graph by adding all the individual edges
    //
    public void AddHyperEdge(ArrayList<Integer> fromList, int to)
    {
        for (int from : fromList)
        {
            this.AddEdge(from, to);
        }
    }

    //
    // Simple heuristic for which we may use graph minors to acquire isomorphisms
    //
    public int NumEdges()
    {
        return numEdges;
    }

    //
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
        return GetLengthHelper(vertices.get(1), 1);
    }
    private int GetLengthHelper(int currentNode, int currentDepth)
    {
        int maxDepth = -1;

        // Get the edges from this node to traverse
        ArrayList<Integer> backwardEdges = null;

        // Get the transpose edges from this node (as we are traversing backward from the goal)
        // This node is a leaf if no edges: return the known depth
        if (!transposeEdgeMap.containsKey(currentNode)) return currentDepth;

        backwardEdges = transposeEdgeMap.get(currentNode);

        // Traverse the edges tracking the max depth
        for (int edge : backwardEdges)
        {
            int tempDepth = GetLengthHelper(edge, currentDepth + 1);
            if (maxDepth < tempDepth) maxDepth = tempDepth;
        }

        return maxDepth;
    }

    //
    // General graph traversal assuming a DAG; we start at the goal node and walk the transpose edges
    //
    // Since this is a DAG, width is defined as the spread of the graph (like a tree) 
    // Use a BFS technique where we continually update the levelWidth variable to ensure we know where the next level starts
    //
    // Since we have an implied 1-1 map between vertices and indices, we work on indices
    //
    public int GetWidth()
    {
        CircularArrayQueue<Integer> worklist = new CircularArrayQueue<Integer>();

        // Add the 'goal' node as a catalyst
// should work because vertices is a list of Integers so vertices.get(0) is an Integer being enqueued to a queue of Integers
        worklist.enqueue(vertices.get(0));

        // width for this level
        int currentLevelWidth = 1;

        // max width for the entire graph
        int maxLevelWidth = 1;

        // For verification purposes, we track the total number of nodes visited
        int sumOfAllWidths = 0;

        //
        // Traverse the entire graph
        //
        while (!worklist.isEmpty())
        { 
            // Traverse an entire level
            int currentLevelAccumulator = 0;
            for (int ell = 0; ell < currentLevelWidth; ell++)
            {
                // Get the next node
                int currentNode = worklist.dequeue();

                // Get the edges from this node to traverse
                List<Integer> backwardEdges = null;

                if (transposeEdgeMap.containsKey(currentNode))
                {
                    backwardEdges = transposeEdgeMap.get(currentNode);

                    // Add all targets to the worklist
                    //						backwardEdges.ForEach(edgeIndex => worklist.Enqueue(edgeIndex));
                    for (Integer edgeIndex : backwardEdges)
                    {
                        worklist.enqueue(edgeIndex);
                    }
                }
                currentLevelAccumulator += (backwardEdges == null) ? 0 : backwardEdges.size();
            }

            // Completed a level; check width values, including if we have a new maxWidth
            if (currentLevelWidth > maxLevelWidth) maxLevelWidth = currentLevelWidth;

            sumOfAllWidths += currentLevelWidth;

            // Next level's number of nodes
            currentLevelWidth = currentLevelAccumulator;
        }

        if (sumOfAllWidths < vertices.size())
        {
            try {
                throw new Exception("Error in width determination: Did not traverse all nodes!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return maxLevelWidth;
    }

    public boolean ContainsCycle()
    {
        // Update the SCCs
        sccs = GetStronglyConnectedComponents();

        // Since all strongly connected components should contain one node, there should be the exact same number of SCCs as vertices.
        return sccs.size() != vertices.size();
    }

    public String GetStronglyConnectedComponentDump()
    {
        sccs = GetStronglyConnectedComponents();

        StringBuilder str = new StringBuilder();
        str.append("SCCs: ");
        int counter = 0;
        for (ArrayList<Integer> scc : sccs)
        {
            str.append("\t" + (counter++) + ": ");
            for (int v : scc)
            {
                str.append(v + " ");
            }
            str.append("");
        }

        return str.toString();
    }

    //
    // Use Tarjan's Algorithm to acquire the Strongly Connected Components of a given directed graph
    //
    private ArrayList<ArrayList<Integer>> GetStronglyConnectedComponents()
    {
        ArrayList<ArrayList<Integer>> stronglyConnectedComponents = new ArrayList<ArrayList<Integer>>();
        Stack<Integer> workStack = new Stack<Integer>();
        int overallIndex = 0;

        int[] sccIndex = new int[vertices.size()];
        int[] lowLink = new int[vertices.size()];

        // Init to -1 for the tracking data for Tarjan's
        for (int i = 0; i < vertices.size(); i++)
        {
            sccIndex[i] = -1;
            lowLink[i] = -1;
        }

        for (int i = 0; i < vertices.size(); i++)
        {
            if (sccIndex[i] < 0)
            {
                StronglyConnectedSub(vertices.get(i), i, overallIndex, workStack, stronglyConnectedComponents, sccIndex, lowLink);
            }
        }

        return stronglyConnectedComponents;
    }

    private void StronglyConnectedSub(int vertex, int vertexIndex, int overallIndex, Stack<Integer> workStack, ArrayList<ArrayList<Integer>> stronglyConnectedComponents, int[] sccIndex, int[] lowLink)
    {
        //
        // Define the current vertex reachability
        //
        sccIndex[vertexIndex] = overallIndex;
        lowLink[vertexIndex] = overallIndex;
        overallIndex++;

        workStack.push(vertex);

        ArrayList<Integer> dependencies;
        //          if (edgeMap.TryGetValue(vertex, out dependencies)){
        if (edgeMap.containsKey(vertex)){

            dependencies = edgeMap.get(vertex);

            //
            // Follow each edge in depth-first manner
            //
            for (int dependent : dependencies)
            {
                int dependentIndex = vertices.indexOf(dependent);

                if (sccIndex[dependentIndex] < 0)
                {
                    StronglyConnectedSub(dependent, dependentIndex, overallIndex, workStack, stronglyConnectedComponents, sccIndex, lowLink);
                    lowLink[vertexIndex] = Math.min(lowLink[vertexIndex], lowLink[dependentIndex]);
                }
                else if (workStack.contains(dependent))
                {
                    lowLink[vertexIndex] = Math.min(lowLink[vertexIndex], lowLink[dependentIndex]);
                }
            }
        }

        if (lowLink[vertexIndex] == sccIndex[vertexIndex])
        {
            ArrayList<Integer> scc = new ArrayList<Integer>();
            int w;
            do
            {
                w = workStack.pop();
                scc.add(w);
            } while (vertex != w);

            stronglyConnectedComponents.add(scc);
        }
    }

    //
    // The algorithm loops through each node of the graph, in an arbitrary order, initiating a depth-first search that
    // terminates when it hits any node that has already been visited since the beginning of the topological sort
    //
    // L â†� Empty list that will contain the sorted nodes
    // while there are unmarked nodes do
    //    select an unmarked node n
    //    visit(n) 
    public ArrayList<Integer> TopologicalSort()
    {
        // L â†� Empty list that will contain the sorted elements
        ArrayList<Integer> L = new ArrayList<Integer>();

        // Unmarked
        ArrayList<Integer> unmarked = new ArrayList<Integer>();
        for (Integer vertex : vertices){
            unmarked.add(vertex);
        }

        // Temporarily marked
        ArrayList<Integer> tempMarked = new ArrayList<Integer>();

        // Permanently marked
        ArrayList<Integer> marked = new ArrayList<Integer>();

        while (!unmarked.isEmpty())
        {
            // remove a node n from unmarked
            int n = unmarked.get(0);
            unmarked.remove(0);

            Visit(n, unmarked, tempMarked, marked, L);
        }

        return L;
    }

    // function visit(node n)
    //    if n has a temporary mark then stop (not a DAG)
    //    if n is not marked (i.e. has not been visited yet) then
    //        mark n temporarily
    //        for each node m with an edge from n to m do
    //            visit(m)
    //        mark n permanently
    //        add n to head of L
    private void Visit(int n, ArrayList<Integer> unmarked, ArrayList<Integer> tempMarked, ArrayList<Integer> marked, ArrayList<Integer> L)
    {
        // if n has a temporary mark then stop (not a DAG)
        if (tempMarked.contains(n)) return;

        // if n is not marked (i.e. has not been visited yet) then
        if (!marked.contains(n))
        {
            // mark n temporarily
            tempMarked.add(n);

            // for each node m with an edge from n to m do
            ArrayList<Integer> dependencies;
            //              if (edgeMap.TryGetValue(n, out dependencies)){
            if (edgeMap.containsKey(n)){
                dependencies = edgeMap.get(n);
                for (int dependent : dependencies)
                {
                    Visit(dependent, unmarked, tempMarked, marked, L);
                }
            }

            // mark n permanently
            marked.add(n);
            unmarked.remove(n);

            // add n to head of L
            L.add(n);
        }
    }
}