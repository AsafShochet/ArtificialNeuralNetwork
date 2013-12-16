package Project;

import java.util.*;

import TopologicalSort.DirectedGraph;
import TopologicalSort.TopologicalSort;

//This class represents a Network. 

public class Network
{

    public static final double Default_Weight = 0.0;
    public double totalExperimentsError = 0;
    public static double[] randomValues = new double[] { 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8 };
    public static double[] randomValues2 = new double[] { -0.5, -0.4, -0.3, -0.2, -0.1, 0.1, 0.2, 0.3, 0.4, 0.5 };
    
    private List<PNode> topologicalOrder; // list of nodes by topological order.
    private List<PNode> reversedTopologicalOrder;
    private HashMap<String, PNode> network; // holds all network nodes with node
					    // names as keys.
    private List<PNode> specialNodesToLearn = null; // list of special nodes to
						    // learn

    // new network initializer
    // prepares the new network and set it's dummy root
    public Network(String networkName)
    {
	network = new HashMap<String, PNode>();
    }

    // adds a node with name <name> to this network. if it already exists no
    // action is taken.
    // returns the node.
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

    public PNode AddNode(PNode node)
    {
	if (network.get(node.GetName()) == null)
	    ;
	{

	    network.put(node.GetName(), node);
	}
	return node;
    }

    // links 2 nodes in parent-child relationship.
    public boolean Link(String parent, String child)
    {
	double defaultWeight = ProgramSettings.GetRandomNumberFromList(Network.randomValues2);

	PNode parentNode = network.get(parent);
	PNode childNode = network.get(child);

	if (parentNode == null || childNode == null)
	    return false;

	parentNode.AddDescendant(childNode);
	childNode.AddPredecessor(parentNode, defaultWeight);
	return true;
    }

    // return a node from the network.
    // if it doesn't exist - return null
    public PNode GetNode(String name)
    {
	if (!network.keySet().contains(name))
	    return null;
	return network.get(name);
    }

    // reset properties relevant to the learning process on all nodes
    private void ResetAllNodes()
    {
	for (PNode node : network.values())
	{
	    node.SetIsCalculated(false);
	    node.SetIsInput(false);
	    node.SetExperimentOutputValue(0.0);
	    node.SetIsOutput(false);
	}
    }

    // calculated SE for a node
    public static double CalculateError(double calculatedVal, Double measuredVal)
    {
	return Math.pow(calculatedVal - measuredVal, 2);
    }

    public double GetSumOfWeights()
    {
	double sum = 0.0;
	for (PNode node : network.values())
	    for (double weight : node.GetPredecessors().values())
		sum += weight;
	return sum;
    }

    // BackPropogation algorithm
    public void BackPropogate(Experiment e, PropagationSettings ps)
    {
	for (PNode node : ReversedTopologicalSort())
	{
	    if (node.GetIsOutput())
	    {
		double calculatedValue = node.GetValue();
		double experimentalValue = node.GetExperimentOutputValue();
		double partialDerivative = calculatedValue * (1 - calculatedValue) * (experimentalValue - calculatedValue);
		node.SetPartialDerivative(partialDerivative);
	    }
	}

	for (PNode node : ReversedTopologicalSort())
	{
	    if (node.GetIsHidden())
	    {
		double errorTerm = 0;
		double calculatedValue = node.GetValue();
		for (PNode desc : node.GetDescandants())
		{
		    errorTerm += desc.GetPartialDerivarive() * desc.GetPredecessors().get(node);
		}
		node.SetPartialDerivative(errorTerm * calculatedValue * (1 - calculatedValue));
	    }
	}

	for (PNode node : TopologicalSort())
	{
	    if (!node.GetIsInput())
	    {
		for (PNode pred : node.GetPredecessors().keySet())
		{
		    double currentWeight = node.GetPredecessors().get(pred);
		    double weightChange = ps.GetLearningRate() * node.GetPartialDerivarive() * pred.GetValue();
		    node.GetPredecessors().put(pred, currentWeight + weightChange);
		}
	    }
	}
    }

    public List<PNode> GetUnknownFunctions()
    {
	if (specialNodesToLearn == null)
	{
	    this.specialNodesToLearn = new ArrayList<>();
	    for (PNode p : TopologicalSort())
		if (p.GetIsUnknownFunction())
		    this.specialNodesToLearn.add(p);
	}
	else
	{ // special Nodes to learn is already calculated
	    return specialNodesToLearn;
	}

	return specialNodesToLearn;
    }

    public List<PNode> TopologicalSort()
    {
	if (topologicalOrder != null) // already made a topologically ordered
				      // list
	    return topologicalOrder;

	// converting our network into a directed graph
	DirectedGraph<String> directedGraph = new DirectedGraph<>();
	for (PNode node : network.values())
	{
	    directedGraph.addNode(node.GetName());
	    for (PNode parent : node.GetPredecessors().keySet())
	    {
		directedGraph.addNode(parent.GetName());
		directedGraph.addEdge(parent.GetName(), node.GetName());
	    }
	}

	// sort
	List<String> sortedList = TopologicalSort.sort(directedGraph);
	List<PNode> topSorted = new ArrayList<PNode>();
	for (String nodeName : sortedList)
	    topSorted.add(network.get(nodeName));

	this.topologicalOrder = topSorted;
	return topSorted;

    }

    public List<PNode> ReversedTopologicalSort()
    {
	if (reversedTopologicalOrder != null)
	    return reversedTopologicalOrder;
	else if (topologicalOrder == null)
	    return null;
	else
	{
	    reversedTopologicalOrder = new ArrayList<PNode>();
	    reversedTopologicalOrder.addAll(topologicalOrder);
	    Collections.reverse(reversedTopologicalOrder);
	    return reversedTopologicalOrder;
	}
    }

    public List<Edge> Edges()
    {
	List<Edge> edges = new ArrayList<Edge>();

	List<PNode> topSort = this.TopologicalSort();
	for (PNode p : topSort)
	{
	    if (p.GetPredecessors() != null)
	    {
		for (PNode des : p.GetPredecessors().keySet())
		    edges.add(new Edge(des, p, p.GetPredecessors().get(des)));
	    }
	}
	return edges;
    }

    // get string representing all edges in the network
    public String EdgesWeights()
    {

	String res = "";
	for (Edge e : Edges())
	    res += String.format("edge %s => %s : %f\n", e.source.GetName(), e.destination.GetName(), e.GetWeight());

	return res;
    }

    public void SetTotalError(double totalExperimentsError)
    {
	this.totalExperimentsError = totalExperimentsError;
    }

    public double GetTotalError()
    {
	return totalExperimentsError;
    }

    // print the network
    public void Print()
    {
	System.out.format("Network Summary:\n");
	System.out.format("Total number of nodes is %d:\n", network.keySet().size());
	for (PNode node : network.values())
	{
	    System.out.format("%s\n", node.GetName());
	    System.out.format("Predecessors:\n");
	    for (PNode pred : node.GetPredecessors().keySet())
	    {
		System.out.format("\t%s value: %f defaultValue: %f\n", pred.GetName(), pred.GetValue(), pred.GetDefaultValue());
	    }
	    System.out.format("Decsandants:\n");
	    for (PNode desc : node.GetDescandants())
	    {
		System.out.format("\t%s %f\n", desc.GetName(), desc.GetValue());
	    }
	    System.out.println();
	}
    }

    // learn - feed forward the experiment inputs through the network and then
    // bacbpropogate according to the calculated outputs
    public void Learn(Experiment exp, PropagationSettings ps)
    {
	this.FeedForward(exp);
	this.BackPropogate(exp, ps);
    }

    public void FeedForward(Experiment exp)
    {
	ResetAllNodes();
	SetNodesType(exp);

	// feed forward
	for (PNode node : this.TopologicalSort())
	{
	    if (!node.GetIsInput())
	    { // only over nodes that are not inputs
		try
		{
		    node.Calculate();
		}
		catch (Exception e)
		{
		    System.out.println("feed forward - exception on calculation. Name: " + node.GetName() + "Exception: " + e);
		}
	    }
	}
    }

    // set nodes types (input/output/hidden) according to a given experiment
    private void SetNodesType(Experiment exp)
    {
	Set<String> hidden = new HashSet<String>(network.keySet());
	for (Map.Entry<String, Double> output : exp.outputs.entrySet())
	{
	    Double nodeValue = output.getValue();
	    PNode node = this.GetNode(output.getKey());
	    node.SetExperimentOutputValue(nodeValue);
	    node.SetIsOutput(true);
	    node.SetIsInput(false);
	    hidden.remove(output.getKey());
	}

	for (Map.Entry<String, Double> input : exp.inputs.entrySet())
	{
	    Double nodeValue = input.getValue(); // calculated value
	    PNode node = this.GetNode(input.getKey());
	    node.SetValue(nodeValue);
	    node.SetIsInput(true);
	    node.SetIsCalculated(true);
	    hidden.remove(input.getKey());
	}

	for (String hid : hidden)
	{
	    PNode node = this.GetNode(hid);
	    node.SetIsHidden(true);
	}
    }

    public void ResetNetwork()
    {
	for (PNode p : network.values())
	{
	    for (PNode des : p.GetPredecessors().keySet())
	    {
		p.GetPredecessors().put(des, ProgramSettings.GetRandomNumberFromList(randomValues2));
	    }
		
	}
	ResetAllNodes();
    }
}
