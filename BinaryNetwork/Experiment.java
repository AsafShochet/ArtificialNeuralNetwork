import java.util.*;

public class Experiment
{
    HashMap<String, Double> fixedNodes;  // nodes that are fixed before
					  // committing the test (input nodes)
    HashMap<String, Double> measuredNodes; // expected results for nodes
					    // after test (output nodes)

    public Experiment(String experiment)
    {
	measuredNodes = new HashMap<String, Double>(); // init
	fixedNodes = new HashMap<String,Double>(); // init
	
	//parse experiment line
	int lineIndex = experiment.indexOf("| |");
	int endIndex = experiment.indexOf(" # #");
	String fixed = experiment.substring(0, lineIndex);
	String expectedResults = experiment.substring(lineIndex, endIndex)
		.replace("| | ", "");
	parseMeasuredValues(expectedResults);
	parseFixedValues(fixed);
    }

    // gets the network, adds the default values + fixed values
    // performs the actual run of the experiment
    public String RunTest(Network network)
    {
	int outputNodesMeasured = 0; // how many nodes we want to measure
	int outputNodesAsExpected = 0; // how many nodes are as we wanted
	network.ResetAllNodes();

	for (Map.Entry<String, Double> nodeEntry : fixedNodes.entrySet())
	{ 
	    double nodeValue = nodeEntry.getValue();
	    PNode node = network.GetNode(nodeEntry.getKey());
	    node.SetValue(nodeValue);
	    node.SetIsFixed(true);
	    node.SetIsProcessed(true);
	}

	for (String nodeName : measuredNodes.keySet())
	{
	    PNode node = network.GetNode(nodeName);
	    try
	    {
		node.Calculate(); // uses javax to calculate for every node its
				   // value by it's predecessors
	    }
	    catch (Exception e)
	    {
		System.out.println("exception on calculation. Name: "
			+ node.GetName() + "Exception: " + e);
	    }
	}

	outputNodesAsExpected = 0;
	outputNodesMeasured = measuredNodes.size();

	for (String node : measuredNodes.keySet())
	{
	    if (fixedNodes.containsKey(node)) //don't count a fixed node.
	    {
		outputNodesMeasured--;
		continue;
	    }
	    if (measuredNodes.get(node)
		    .equals(network.GetNode(node).GetValue()))// ||
							      // network.GetNode(node).GetDefaultState()
							      // == -1 )
		outputNodesAsExpected++;
	}

	return String.format("%s|%s", outputNodesAsExpected,
		outputNodesMeasured);

    }

    public void parseMeasuredValues(String expected)
    {
	if (!expected.isEmpty()) // parsing results value. example: tgfa 1
	// mek12 0 ==> expectedResultValues =
	// {[tgfa:true],[mek12:false]}
	{
	    String[] pairs = expected.split(" ");
	    for (int i = 0; i < pairs.length / 2; i++)
	    {
		String name = pairs[i * 2];
		String value = pairs[i * 2 + 1];
		double val = Double.parseDouble(value);
		measuredNodes.put(name, val);
	    }
	}

    }

    public void parseFixedValues(String fixed)
    {
	if (!fixed.isEmpty()) // parsing fixed value. example: tgfa 1
			      // mek12 0
	// ==>fixedNodes = {[tgfa:true],[mek12:false]}
	{
	    String[] pairs = fixed.split(" ");
	    for (int i = 0; i < pairs.length / 2; i++)
	    {
		String name = pairs[i * 2];
		String value = pairs[i * 2 + 1];
	
		double val = Double.parseDouble(value);
		fixedNodes.put(name, val);
	    }
	}

    }
}
