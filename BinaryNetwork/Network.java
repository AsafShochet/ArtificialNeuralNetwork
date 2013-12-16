import java.util.*;


public class Network
{
    private HashMap<String, PNode> network; // holds all network nodes with node names as keys.
    private List<PNode> topologicalOrdered; // list of nodes by topological
	// order.
    
    // new network initializer
    // prepares the new network and set it's dummy root
    public Network(String networkName)
    {
	network = new HashMap<String, PNode>();
    }

    // adds a node with name <name> to this network. if it already exists no action is taken.
    // returns the node with name <name>.
    public PNode AddNode(String name)
    {
	PNode node = network.get(name);
	if (node == null)
	{
	    node = new PNode(name);
	    network.put(name, node);
	}

	return node;
    }

    //links 2 nodes in parent-child tree relationship
    public boolean Link(String parent, String child)
    {
	PNode parentNode = network.get(parent);
	PNode childNode = network.get(child);

	if (parentNode == null || childNode == null)
	    return false;

	parentNode.AddChild(childNode);
	return true;
    }
    
    // returns a node from the network.
    // if it doesn't exist - returns null
    public PNode GetNode(String name)
    {
	return network.get(name);
    }

    public void ResetAllNodes()
    {
	for (PNode node : network.values())
	    node.ResetValue();
    }
    

	public List<PNode> TopologicalSorted() {
		if (topologicalOrdered != null) // already made a topoligically ordered
										// list
			return topologicalOrdered;

		// converting our network into a directed graph
		DirectedGraph<String> directedGraph = new DirectedGraph<>();
		for (PNode node : network.values()) {
			directedGraph.addNode(node.GetName());
			for (PNode parent : node.GetPredecessors()) {
				directedGraph.addNode(parent.GetName());
				directedGraph.addEdge(parent.GetName(), node.GetName());
			}
		}

		// topological sorting
		List<String> sortedList = TopologicalSort.sort(directedGraph);
		List<PNode> topSorted = new ArrayList<PNode>();
		for (String nodeName : sortedList)
			topSorted.add(network.get(nodeName));

		this.topologicalOrdered = topSorted;
		return topSorted;

	}

}
