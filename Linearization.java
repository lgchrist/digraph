package digraph;

import java.util.ArrayList;

public class Linearization<T>
{
    private ArrayList<Node<T>> nodes;
    
    public Linearization()
    {
        nodes = new ArrayList<Node<T>>();
    }
    
    public void addNode(Node<T> newNode)
    {
        nodes.add(newNode);
    }
    
    public Node<T> getNode(int index)
    {
        return nodes.get(index);
    }
    
    public ArrayList<Node<T>> getNodes()
    {
        return nodes;
    }
    
    public Node<T> removeLast()
    {
        Node<T> lastNode = nodes.get(nodes.size() - 1);
        nodes.remove(lastNode);
        return lastNode;
    }
    
    public String toASCIIString()
    {
        String linearization = new String();
        
        for(Node currNode: nodes)
        {
            Integer charInt = currNode.getId() + 33;
            char ASCIIChar = (char) Character.toLowerCase(charInt);
            linearization += ASCIIChar;
        }
        
        return linearization;
    }
    
    public int[] toArray()
    {
        int[] ary = new int[nodes.size()];
        int index = 0;
        for(Node<T> currNode: nodes)
        {
            ary[index] = currNode.getId();
            index++;
        }
        return ary;
    }
    
    @Override
    public String toString()
    {
        return nodes.toString();
    }
}
