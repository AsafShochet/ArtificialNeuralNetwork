import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

//this class includes the main logic required for building and simulating the network 
public class NetworkLogic
{
    private static NetworkLogic instance;
    private Network proteinsHash;

    //create NetworkLogic as Singleton.
    public static NetworkLogic GetInstance()
    {
	if (instance == null)
	    return new NetworkLogic();
	else
	    return instance;
    }

    //read and parse exp_file
    public List<Experiment> ReadExperimentsFromFile(String expPath)
    {
	List<String> experiments = new ArrayList<String>();
	String input;
	
	//open and read
	try
	{
	    BufferedReader in = new BufferedReader(new FileReader(expPath));
	    while ((input = in.readLine()) != null)
	    {
		if (input.contains("| |") && (input.contains("# #")))
		    experiments.add(input); // add the experiment String to the
					    // experiments list
	    }
	    in.close();
	}
	catch (Exception e)
	{
	    System.out.println("exception on reading file : " + e);
	    return null;
	}

	//build Experiment object for every experiment line
	List<Experiment> experimentList = new ArrayList<Experiment>();

	for (String experiment : experiments)
	{
	    experimentList.add(new Experiment(experiment));	    
	}
	
	return experimentList;
    }

    //build a network from the given reac and spec files
    public Network BuildNetworkFromFiles(String reacPath, String specPath)
    {
	proteinsHash = new Network("Main");
	String input;
	
	//open and read
	try
	{
	    // handle reac file
	    BufferedReader in = new BufferedReader(new FileReader(reacPath));
	    while ((input = in.readLine()) != null)
	    {
		HandleReacLine(input);
	    }
	    in.close();

	    // handle spec file
	    in = new BufferedReader(new FileReader(specPath));
	    while ((input = in.readLine()) != null)
	    {
		HandleSpecLine(input);
	    }
	    in.close();

	}
	catch (Exception e)
	{
	    System.out.println("exception on reading file : " + e);
	    return null;
	}

	return proteinsHash;
    }

    //handles a specific line from the spec file
    private void HandleSpecLine(String input)
    {
	String[] nodeSpec = input.split(" ");
	if (!nodeSpec[1].toLowerCase().contains("nan"))
	{
	    byte val = Byte.parseByte(nodeSpec[1]) == 0 ? (byte)0 : (byte)1;
	    proteinsHash.GetNode(nodeSpec[0]).SetDefaultState(val);
	}
    }

    //handles a specific line from the reac file
    private void HandleReacLine(String input)
    {

	String[] splittedTerm = input.split("=");
	if (splittedTerm.length == 1)
	{
	    proteinsHash.AddNode(splittedTerm[0]);
	    return;
	}
	else if (splittedTerm[0].equals(""))
	{
	    proteinsHash.AddNode(splittedTerm[1]);
	    return;
	}
	else
	{
	    PNode out = proteinsHash.AddNode(splittedTerm[1]);
	    String[] predecessors = splittedTerm[0].split("\\+!|\\+|!");
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
