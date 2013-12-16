package Project;

import java.util.*;

public class Experiment
{
    int id;
    PropagationSettings ps = new PropagationSettings();

    HashMap<String, Double> inputs; // experiment input nodes
    HashMap<String, Double> outputs; // experiment output nodes
    HashSet<String> hidden; // nodes that are neither inputs nor outputs

    public Experiment(String experiment, int id)
    {
	// initialize
	outputs = new HashMap<String, Double>();
	inputs = new HashMap<String, Double>();
	hidden = new HashSet<String>();

	// parse experiment line
	int lineIndex = experiment.indexOf("| |");
	int endIndex = experiment.indexOf(" # #");
	String fixed = experiment.substring(0, lineIndex);
	String expectedResults = experiment.substring(lineIndex, endIndex).replace("| | ", "");
	parseMeasuredValues(expectedResults);
	parseFixedValues(fixed);
	this.setId(id);
    }

    // parse output nodes
    public void parseMeasuredValues(String expected)
    {
	if (!expected.isEmpty()) 
	{
	    String[] pairs = expected.split(" ");
	    for (int i = 0; i < pairs.length / 2; i++)
	    {
		String name = pairs[i * 2];
		String valueString = pairs[i * 2 + 1];
		double value;
		try
		{
		    value = Double.parseDouble(valueString);
		}
		catch (NumberFormatException e)
		{
		    System.out.println("Could not parse value for node: " + name + " from exp_file. failed parsing text: " + valueString);
		    return;
		}

		PropagationSettings ps = new PropagationSettings();
		if (ps.ReplaceOutputsWithFraction())
		    value = PropagationSettings.GetFractionValue(value);
		outputs.put(name, value);
	    }
	}
    }

    // parse input nodes
    public void parseFixedValues(String fixed)
    {
	if (!fixed.isEmpty())
	{
	    String[] pairs = fixed.split(" ");
	    for (int i = 0; i < pairs.length / 2; i++)
	    {
		String name = pairs[i * 2];
		String valueString = pairs[i * 2 + 1];
		double value;
		try
		{
		    value = Double.parseDouble(valueString);
		}
		catch (NumberFormatException e)
		{
		    System.out.println("Could not parse value for node: " + name + " from exp_file. failed parsing text: " + valueString);
		    return;
		}

		if (PropagationSettings.ReplaceInputsAndDefaultsWithFraction())
		{
		    value = PropagationSettings.GetFractionValue(value);
		}

		inputs.put(name, value);
	    }
	}

    }

    // get the difference between the model prediction and the experiment
    // results
    public double GetTotalError(Network network)
    {
	network.FeedForward(this);
	double globalError = 0;
	for (String nodeName : outputs.keySet())
	{
	    PNode node = network.GetNode(nodeName);
	    try
	    {

		if (ps.ConvertToBooleanOnErrorCalculation())
		{
		    globalError += Math.pow((PropagationSettings.GetBooleanValue(node.GetValue()) - outputs.get(nodeName)), 2);
		}
		else
		{
		    globalError += Math.pow((node.GetValue() - outputs.get(nodeName)), 2);
		}

	    }
	    catch (Exception e)
	    {
		System.out.println("Get total error : exception on calculation. Name: " + node.GetName() + "Exception: " + e);
	    }
	}
	return globalError;

    }

    public int getId()
    {
	return id;
    }

    public void setId(int id)
    {
	this.id = id;
    }

    public String ToString()
    {
	StringBuilder builder = new StringBuilder();
	builder.append("Inputs:");
	for (Map.Entry<String, Double> key : inputs.entrySet())
	{
	    builder.append(String.format("%s %f  ", key.getKey(), key.getValue()));
	}
	builder.append("\nOutputs:");
	for (Map.Entry<String, Double> key : outputs.entrySet())
	{
	    builder.append(String.format("%s %f  \n", key.getKey(), key.getValue()));
	}
	return builder.toString();
    }

    public HashMap<String, Double> GetOutputs()
    {
	return outputs;
    }
}
