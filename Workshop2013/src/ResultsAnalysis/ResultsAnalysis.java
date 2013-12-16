package ResultsAnalysis;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import Project.Edge;
import Project.Experiment;
import Project.Network;
import Project.PNode;
import Project.ProgramSettings;
import org.apache.commons.lang3.StringUtils;

//helper class - used for results analysis
public class ResultsAnalysis
{

    private static boolean writeToScreen = true;
    public static boolean SortOutputsBySE = true; // sort outputs nodes in
						  // analysis files by the SE,
						  // from high to low.
    public static String currentAnalysisFolder = "";

    private static HashMap<String, PrintWriter> FileWriters = new HashMap<String, PrintWriter>();

    public static String InitializeAnalysisFile(String fileName, String handle)
    {
	currentAnalysisFolder = CreateFolder(ProgramSettings.resultsFolder, ProgramSettings.currentRunResultsfolder);
	String analysisFileName = currentAnalysisFolder + fileName;
	File f = new File(analysisFileName);

	try
	{
	    // Create the empty file
	    if (f.exists())
		f.delete();

	    f.createNewFile();

	}
	catch (SecurityException e)
	{
	    System.err.format("security exception when creating\\deleting file named %s" + fileName);
	}
	catch (IOException x)
	{
	    // Some other sort of failure, such as permissions.
	    System.err.format("createFile error: %s%n", x);
	}

	try
	{
	    FileWriters.put(handle, new PrintWriter(analysisFileName, "UTF-8"));
	}
	catch (Exception e)
	{
	    System.out.println("error making a new PrintWriter\n" + e.toString());
	}

	return analysisFileName;
    }

    public static void CloseAnalysisFile(String handle)
    {
	FileWriters.get(handle).close();
    }

    public static void AddToAnalysis(String handle, String string)
    {
	FileWriters.get(handle).println(string);
	if (writeToScreen)
	{
	    System.out.println(string);
	}
    }

    public static void PrintTotalErrorBySquareDifferences(Network network, List<Experiment> experiments)
    {
	double sum = 0;
	for (Experiment exp : experiments)
	{ // collecting errors for each Pnode
	    network.FeedForward(exp);
	    for (String pName : exp.GetOutputs().keySet())
	    {
		PNode node = network.GetNode(pName);
		double pValue = node.GetValue();
		double expectedPValue = exp.GetOutputs().get(pName);
		sum += Math.pow((pValue - expectedPValue), 2);
	    }
	}
	System.out.println("Total sum of square differences : " + sum);
    }

    public static List<PNodeExperimentResults> PNodesSortedByTotalError(HashMap<PNode, PNodeExperimentResults> results)
    {

	Comparator<PNodeExperimentResults> comp = new Comparator<PNodeExperimentResults>()
	{

	    @Override
	    public int compare(PNodeExperimentResults o1, PNodeExperimentResults o2)
	    {
		double o1Distance = o1.GetAverageDistanceFromOutput();
		double o2Distance = o2.GetAverageDistanceFromOutput();

		if (o1Distance < o2Distance)
		    return -1;
		if (o1Distance > o2Distance)
		    return 1;
		return 0;
	    }
	};

	List<PNodeExperimentResults> resultsList = new ArrayList<PNodeExperimentResults>();
	for (PNodeExperimentResults p : results.values())
	    resultsList.add(p);

	Collections.sort(resultsList, comp); // sort
	return resultsList;
    }

    public static void PrintExperimentResultTable(Network network, List<Experiment> experiments)
    {
	for (Experiment exp : experiments)
	{ // collecting errors for each Pnode
	    network.FeedForward(exp);
	    for (String pName : exp.GetOutputs().keySet())
	    {
		PNode node = network.GetNode(pName);
		double pValue = node.GetValue();
		double expectedPValue = exp.GetOutputs().get(pName);
		int experimentIndex = experiments.indexOf(exp);

		System.out.println(experimentIndex + " : " + pName + " : " + pValue + " : " + expectedPValue); // print
													       // the
													       // node
													       // output
													       // of
													       // this
													       // experiment
	    }

	}

    }

    public static void CreateGraphFile(String filename, Network network)
    {
	XgmmlHandler.CreateGraphFile(filename, network);
    }

    public static void CreateWeightsFile(String filename, LearningNetworkResult result)
    {

	File f = new File(filename);

	try
	{
	    // Create the empty file

	    if (f.exists())
		f.delete();

	    f.createNewFile();

	}
	catch (SecurityException e)
	{
	    System.err.format("security exception when creating\\deleting file named %s" + filename);
	}
	catch (IOException x)
	{
	    // Some other sort of failure, such as permissions.
	    System.err.format("createFile error: %s%n", x);
	}
	PrintWriter writer = null;

	try
	{
	    writer = new PrintWriter(filename, "UTF-8");
	}
	catch (Exception e)
	{
	    System.out.println("error making a new PrintWriter\n" + e.toString());
	}

	List<Edge> edgesList = result.bestModel.Edges();
	for (Edge e : edgesList)
	{
	    String source = e.source.GetName();
	    String dest = e.destination.GetName();
	    String weight = e.GetWeight().toString();
	    writer.println(String.format("%s:%s=%s", source, dest, weight)); // edge
									     // a->b
									     // with
									     // weight
									     // 5.34,
									     // will
									     // be
									     // a
									     // line
									     // with
									     // text:
									     // "a:b=5.34"
									     // (without
									     // the
									     // quoutes)

	}

	writer.print("Measured experiments:"); // prints Mesaured expriments:\n
					       // 2,4,6,5
	for (int exp : result.measuredExperimentNumbers)
	{
	    writer.print(exp);
	    if (result.measuredExperimentNumbers.indexOf(exp) < result.measuredExperimentNumbers.size() - 1)
		writer.print(",");
	    else
		writer.println();
	}

	writer.close();

    }

    public static String CreateFolder(String mainpath, String newFolder)
    {
	File folderName = new File(mainpath + "\\" + newFolder);

	// if the directory does not exist, create it
	if (!folderName.exists())
	{
	    folderName.mkdir();
	}
	return folderName + "\\";
    }

    public static void WriteResultsToFile(Network network, List<Experiment> experiments)
    {
	int index = 1;
	double expError = 0;
	List<String> outputsSorted;

	for (Experiment exp : experiments)
	{
	    network.FeedForward(exp);
	    outputsSorted = SortOutputsBySE(exp, network);
	    AddToAnalysis("general", String.format("Experiment no %d", index));
	    for (String nodeName : outputsSorted)
	    {
		PNode node = network.GetNode(nodeName);
		AddToAnalysis("general", String.format("protein name: %s | calculated value: %f | experimental value: %f", node.GetName(),
			node.GetValue(), exp.GetOutputs().get(nodeName)));
		expError += Math.pow(node.GetValue() - exp.GetOutputs().get(nodeName), 2);

	    }
	    index++;
	    AddToAnalysis("general", String.format("Total Error for exp %d: %f\n", index, expError));
	    expError = 0;
	}
    }

    private static List<String> SortOutputsBySE(Experiment exp, Network network)
    {
	final HashMap<String, Double> outputsErrors = new HashMap<String, Double>();
	List<String> outputsOrdered = new ArrayList<String>();

	// keep all SE for output nodes
	for (String outputName : exp.GetOutputs().keySet())
	{
	    outputsOrdered.add(outputName);
	    PNode node = network.GetNode(outputName);
	    outputsErrors.put(outputName, Math.pow(node.GetValue() - exp.GetOutputs().get(outputName), 2));
	}

	// sort outputs by their SE
	if (ResultsAnalysis.SortOutputsBySE)
	{
	    Collections.sort(outputsOrdered, new Comparator<String>()

	    {
		public int compare(String node1, String node2)
		{
		    double node1SE = outputsErrors.get(node1);
		    double node2SE = outputsErrors.get(node2);
		    return (node1SE > node2SE ? -1 : (node1SE == node2SE ? 0 : 1));
		}
	    });
	}

	return outputsOrdered;
    }

    // returns string representing all nodes and function that lead to the
    // specified node
    public static String TrackNode(String nodeName, Network network)
    {
	ArrayList<PNode> allNodes = new ArrayList<PNode>();
	StringBuilder builder = new StringBuilder();
	PNode node = network.GetNode(nodeName);
	if (node == null)
	    return "";
	builder.append(String.format("Tracking Node %s:\n\n", nodeName, node.GetValue()));
	TrackNodeRec(node, 0, allNodes);
	for (PNode pred : allNodes)
	{
	    builder.append(String.format("name: %s value %f ", pred.GetName(), pred.GetValue()));
	    if (node.GetPredecessorsList().size() != 0)
	    {
		ArrayList<String> terms = new ArrayList<String>();
		for (String exp : pred.GetTerms())
		{
		    terms.add(String.format("(%s)", exp));
		}
		builder.append(String.format("Function: %s\n", StringUtils.join(terms, "||")));
	    }

	}

	builder.append(String.format("Node %s is affected by a total number of %d nodes.\n", node.GetName(), allNodes.size()));
	return builder.toString();
    }

    private static String TrackNodeRec(PNode node, int level, ArrayList<PNode> predecessorsList)
    {
	if (!predecessorsList.contains(node))
	{
	    predecessorsList.add(node);
	    for (PNode pred : node.GetPredecessorsList())
	    {
		TrackNodeRec(pred, level + 1, predecessorsList);
	    }
	    return "";
	}
	else
	    return "";
    }

    // return a string indicating max number of times each node got the same
    // value.
    public static String MaximumValuesRepetition(Network network, List<Experiment> experiments)
    {
	HashMap<String, Integer> maximumRepetitions = new HashMap<>();
	HashMap<String, HashMap<Double, Integer>> sizes = new HashMap<>();
	HashMap<String, Double> popoularResult = new HashMap<>();

	for (Experiment exp : experiments)
	{
	    network.FeedForward(exp);
	    for (String node : exp.GetOutputs().keySet())
	    {
		int numOfErrors;
		if (!sizes.containsKey(node))
		    sizes.put(node, new HashMap<Double, Integer>()); // init
								     // hashmap

		double calculated = network.GetNode(node).GetValue();
		if (sizes.get(node).containsKey(calculated))
		{
		    numOfErrors = sizes.get(node).get(calculated);
		    sizes.get(node).put(calculated, numOfErrors + 1); /*
								       * update
								       * sizes
								       * [node
								       * ][value
								       * ]
								       */

		}
		else
		{
		    sizes.get(node).put(calculated, 1);
		    numOfErrors = 0;
		}

		if (!maximumRepetitions.containsKey(node)) /*
							    * update value of
							    * maximum
							    * repetitions
							    */
		{
		    maximumRepetitions.put(node, 1);
		}
		else if (maximumRepetitions.get(node) < numOfErrors + 1) /*
									  * popular
									  * result
									  * updated
									  */
		{
		    maximumRepetitions.put(node, numOfErrors + 1);
		    popoularResult.put(node, calculated);
		}
	    }

	}
	String result = "Maximum number of repetitions in different experiments per output node:\n"
		+ "|| Node\t || Max repetitions\t || Value\t ||\n";

	for (String node : maximumRepetitions.keySet())
	{
	    int maxRep = maximumRepetitions.get(node);
	    double maxRepValue = popoularResult.get(node);
	    result += String.format("|| %s\t || %d\t || %f\t ||\n", node, maxRep, maxRepValue);
	}

	// System.out.format("Total errors per node:\n %s", result);
	return result;
    }

    public static String ExploreSingleNode(String nodeToExplore, Network network, List<Experiment> experiments)
    {

	HashMap<Double, Integer> valueSizes = new HashMap<>();
	HashMap<Double, List<Integer>> valuesInExperiment = new HashMap<>();

	for (Experiment exp : experiments)
	{
	    network.FeedForward(exp);
	    if (exp.GetOutputs().keySet().contains(nodeToExplore))
	    {
		double calculated = network.GetNode(nodeToExplore).GetValue();
		if (valueSizes.containsKey(calculated))
		{
		    int numOfErrors = valueSizes.get(calculated);
		    valueSizes.put(calculated, numOfErrors + 1);
		    valuesInExperiment.get(calculated).add(exp.getId());
		}
		else
		{
		    valueSizes.put(calculated, 1);
		    valuesInExperiment.put(calculated, new ArrayList<Integer>());
		    valuesInExperiment.get(calculated).add(exp.getId());
		}
	    }

	}
	String result = "Calculated values for node " + nodeToExplore + " in different experiments:\n"
		+ "|| Value\t || Number of times\t|| Experiments no.\t ||\n";
	for (Double val : valueSizes.keySet())
	{
	    String expList = "";
	    for (int i : valuesInExperiment.get(val))
		expList += i + " ";

	    result += String.format("|| %f\t || %d\t\t || %s\t||\n", val, valueSizes.get(val), expList);
	}

	result += "Values of " + nodeToExplore + " predecessors in experiments:\n";
	for (Experiment exp : experiments)
	{
	    network.FeedForward(exp);
	    result += "Experiment ID: " + exp.getId() + "\n";
	    result += ResultsAnalysis.TrackNode(nodeToExplore, network);
	}
	// System.out.format("Total errors per node:\n %s", result);
	return result;
    }

    public static String ErrorsPerNode(Network network, List<Experiment> experiments)
    {
	HashMap<String, Double> errors = new HashMap<>();
	StringBuilder builder = new StringBuilder();

	builder.append("Total Error Per Node:\n");

	for (Experiment exp : experiments)
	{
	    network.FeedForward(exp);
	    for (String nodeName : exp.GetOutputs().keySet())
	    {
		double calculated = network.GetNode(nodeName).GetValue();
		double experimental = exp.GetOutputs().get(nodeName);
		if (errors.containsKey(nodeName))
		{
		    Double nodeErrors = errors.get(nodeName);
		    errors.put(nodeName, nodeErrors + Math.pow(calculated - experimental, 2));
		}
		else
		{
		    errors.put(nodeName, Math.pow(calculated - experimental, 2));
		}
	    }
	}

	for (String name : errors.keySet())
	{
	    builder.append(name + " : " + errors.get(name) + "\n");
	}

	return builder.toString();

    }

    // returns string representing total error per experiment
    public static String ErrorsPerExperiment(Network network, List<Experiment> experiments)
    {
	StringBuilder builder = new StringBuilder();
	double expError = 0;
	int i = 1;
	builder.append("Total Error Per Experiment:\n");

	for (Experiment exp : experiments)
	{
	    expError = 0;

	    network.FeedForward(exp);
	    for (String nodeName : exp.GetOutputs().keySet())
	    {
		double calculated = network.GetNode(nodeName).GetValue();
		double experimental = exp.GetOutputs().get(nodeName);
		expError += Math.pow(calculated - experimental, 2);
	    }
	    builder.append(String.format("exp %d: %f\n", i, expError));

	    i++;
	}

	return builder.toString();

    }

    //track a single node through all experiments
    public static String TrackNodeValuesInExperiments(String nodeName, Network model, List<Experiment> experiments)
    {
	String result = "";

	for (Experiment exp : experiments)
	{
	    model.FeedForward(exp);
	    result += String.format("Node: %s, Ex : %d, Value : %f.\n", nodeName, exp.getId(), model.GetNode(nodeName).GetValue());
	}
	return result;
    }

}
