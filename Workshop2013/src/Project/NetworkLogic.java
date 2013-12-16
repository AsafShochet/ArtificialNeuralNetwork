package Project;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

//this class includes the main logic required for building the network 
public class NetworkLogic
{
    private static NetworkLogic instance;
    private Network proteinsHash;

    // create NetworkLogic as Singleton.
    public static NetworkLogic GetInstance()
    {
	if (instance == null)
	    return new NetworkLogic();
	else
	    return instance;
    }

    // read and parse the experiments file
    public List<Experiment> ReadExperimentsFromFile(String expPath)
    {
	List<String> experiments = new ArrayList<String>();
	String input;

	try
	{
	    BufferedReader in = new BufferedReader(new FileReader(expPath));
	    while ((input = in.readLine()) != null)
	    {
		if (input.contains("| |") && (input.contains("# #")))
		    experiments.add(input); 
	    }
	    in.close();
	}
	catch (Exception e)
	{
	    System.out.println("exception on reading file : " + e);
	    return null;
	}

	// build Experiment object for every experiment line
	List<Experiment> experimentList = new ArrayList<Experiment>();

	for (String exp : experiments)
	{
	    experimentList.add(new Experiment(exp, experiments.indexOf(exp)));
	}

	return experimentList;
    }

    // build a network from the given reactions and specifications files
    public Network BuildNetworkFromFiles(String reacPath, String specPath)
    {
	proteinsHash = new Network("Main");
	String input;
	String fileToRead = "";

	try
	{
	    //reactions file
	    fileToRead = reacPath;

	    BufferedReader in = new BufferedReader(new FileReader(fileToRead));
	    while ((input = in.readLine()) != null)
	    {
		HandleReacLine(input);
	    }
	    in.close();

	    //specifications file
	    fileToRead = specPath;

	    in = new BufferedReader(new FileReader(fileToRead));
	    while ((input = in.readLine()) != null)
	    {
		HandleSpecLine(input);
	    }
	    in.close();

	}
	catch (Exception e)
	{
	    System.out.println("exception on reading file : " + fileToRead
		    + "\n" + e);
	    return null;
	}

	return proteinsHash;
    }

    //builds the network with specific weights
    public Network BuildNetworkFromFiles(String reacPath, String specPath,
	    String weightsPath)
    {
	Network network = BuildNetworkFromFiles(reacPath, specPath);
	String input;
	String fileToRead = weightsPath;

	BufferedReader in = null;
	try
	{
	    in = new BufferedReader(new FileReader(fileToRead));
	}
	catch (FileNotFoundException e)
	{
	    e.printStackTrace();
	}
	try
	{
	    while ((input = in.readLine()) != null)
	    {
		if (input.contains(":") && input.contains("="))
		    HandleWeightLine(input);

	    }
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	}
	finally
	{
	    try
	    {
		in.close();
	    }
	    catch (IOException e)
	    {

		e.printStackTrace();
	    }
	}
	return network;
    }

    private void HandleWeightLine(String input)
    {
	if (input.isEmpty())
	    return;
	String[] weights = input.split(" ");

	for (String edgePair : weights)
	{
	    String source;
	    String destination;
	    double value;
	    String nodes = edgePair.split("=")[0];
	    source = nodes.split(":")[0];
	    destination = nodes.split(":")[1];
	    try
	    {
		source = nodes.split(":")[0];
		destination = nodes.split(":")[1];
	    }
	    catch (Exception e)
	    {
		System.out.println("Could not parse value for edge: " + nodes
			+ " from weights file.");
		return;
	    }

	    try
	    {
		String strValue = edgePair.split("=")[1];
		value = Double.parseDouble(strValue);
	    }
	    catch (NumberFormatException e)
	    {
		System.out.println("Could not parse value for edge: " + nodes
			+ " from weights file.");
		return;
	    }

	    if (proteinsHash.GetNode(destination) == null)
		System.out
			.println("Could not find node : " + destination + ".");
	    PNode sourceNode = proteinsHash.GetNode(source);
	    PNode destinationNode = proteinsHash.GetNode(destination);
	    destinationNode.UpdateEdgeWeightFrom(sourceNode, value);
	}

    }

    // handles a specific line from the spec file
    private void HandleSpecLine(String input)
    {
	if (input.isEmpty())
	    return;

	String[] nodeSpec = input.split(" ");
	if (!nodeSpec[1].toLowerCase().contains("nan"))
	{
	    double value = Double.parseDouble(nodeSpec[1]);
	    //value = PropogationSettings.GetNormalizedValue(Double.parseDouble(nodeSpec[1]));
	    if (PropagationSettings.ReplaceInputsAndDefaultsWithFraction())
	    {
		value = PropagationSettings.GetFractionValue(value);
	    }
	   
	    proteinsHash.GetNode(nodeSpec[0]).SetDefaultValue(value);
	}
    }

    // handles a specific line from the reac file
    private void HandleReacLine(String input)
    {
	if (input.isEmpty())
	    return;

	String[] splittedTerm = input.split("=");
	if (splittedTerm.length == 1)
	{
	    proteinsHash.AddNode(splittedTerm[0]); // only function input is given
	    return;
	}
	else if (splittedTerm[0].equals("")) // only function output is given
	{
	    proteinsHash.AddNode(splittedTerm[1]);
	    return;
	}
	else
	{
	    PNode out = proteinsHash.AddNode(splittedTerm[1]); //both input and output of the function are given
	    String[] predecessors;
	    if (splittedTerm[0].contains("*"))
	    {
		out.SetIsUnknownFunction(true); //output of an unknown function
		predecessors = splittedTerm[0].split("\\*!|\\*|!");
	    }
	    else
	    {
		predecessors = splittedTerm[0].split("\\+!|\\+|!");
	    } 
	    
	    for (String pred : predecessors)
	    {
		if (!pred.equals(""))
		{
		    proteinsHash.AddNode(pred);
		    proteinsHash.Link(pred, splittedTerm[1]);
		}
	    }

	    out.AddExpression(splittedTerm[0]);
	}
    }
}
