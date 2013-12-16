package Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

//Represents a single node (protein)
public class PNode
{
    private String name;
    private HashMap<PNode, Double> predecessors;
    private List<PNode> predecessorList;
    private HashSet<PNode> descendants;
    private boolean isInput;
    private boolean isOutput;
    private double outputValueOfExperiment;
    private double value = 0.0;
    private boolean isCalculated = false;
    private double defaultValue = 0.0;
    private double lastError = 0;
    private boolean isUnknownFunction;
    private boolean isHidden;
    private double partialDerivarive = 0;
    private ArrayList<String> predExpressions = new ArrayList<String>();

    PropagationSettings ps = new PropagationSettings();

    public String GetName()
    {
	return name;
    }

    public PNode(String name)
    {
	this.name = name;
	this.descendants = new HashSet<PNode>();
	this.predecessors = new HashMap<PNode, Double>();
    }

    // returns the node's predecessors list
    public HashMap<PNode, Double> GetPredecessors()
    {
	return predecessors;
    }

    public double GetValue()
    {
	return value;
    }

    public int GetNumberOfPredecessors()
    {
	return predecessors.size();
    }

    public void SetValue(double newValue)
    {
	value = newValue;
    }

    public List<PNode> GetPredecessorsList()
    {
	if (predecessorList != null)
	    return predecessorList;
	predecessorList = new ArrayList<>();
	for (PNode pred : predecessors.keySet())
	    predecessorList.add(pred);

	return predecessorList;
    }

    //return true if this node is an input node for the currently used experiment
    public Boolean GetIsInput()
    {
	return isInput;
    }

    public void SetIsInput(boolean value)
    {
	this.isInput = value;
    }

    public HashSet<PNode> GetDescandants()
    {
	return descendants;
    }

    //calculate node's value based on it's predecessors
    public double Calculate()
    {

	if (this.isCalculated)
	{
	    return this.value;
	}

	this.isCalculated = true;
	if (this.predecessors.size() == 0)
	{
	    this.value = this.defaultValue;
	    return this.value;
	}
	switch (ps.GetCalculationMethod().toLowerCase())
	{
	case "boolean":
	{
	    return CalculateAsBoolean();
	}
	case "neural":
	{
	    return CalculateAsNeural();
	}
	case "mixed":
	{
	    if (this.GetIsUnknownFunction()) // unknown function - learn
		return CalculateAsNeural();
	    
	    else if (GetIsHasNeuralPredecessor()) //known function which one or more of it's argument are not boolean - simulate boolean calculation
		return CalculateAsSigmoidalBooleanSimulation();
	    
	    else
		return CalculateAsBoolean(); //known function which all it's arguments are boolean - just use the know function
	}

	default:
	    return this.defaultValue;
	}
    }
    
    //simulate a boolean calculation using a Sigmoid function
    private double CalculateAsSigmoidalBooleanSimulation()
    {
	List<Double> termsValues = new ArrayList<Double>();
	double val = 0;
	
	for (String term : predExpressions)
	{
	    val = SimulateAndGate(term);
 	    termsValues.add(val);
	}
	
	this.SetValue(SimulateOrGate(termsValues));
	if (this.value < 0.5) 
	    ProgramSettings.booleansigmoidalCalculationsZero++;
	else
	    ProgramSettings.booleansigmoidalCalculationsOne++;
	return this.value;
    }
    
    private double SimulateOrGate(List<Double> termsValues)
    {
	/*double sum = 0;
	for (Double termValue : termsValues)
	{
	    sum+=10*termValue;
	}
	return ps.sigmoid.Calculate(sum, 5);*/ 
	
	return Sigmoid.SimulateOrGate(termsValues, 10);
    }
    
    private Double SimulateAndGate(String term)
    {
	
	HashMap<String, Double> predValues = new HashMap<>();
	for (PNode pred : GetPredecessorsList())
	{
	    predValues.put(pred.GetName(), pred.GetValue());
	}
	
	List<Double> predecessorsValues = new ArrayList<Double>();
	double sum = 0;

	String[] args = term.split("\\+|\\*");
	for (String predName : args)
	{
	    Double predValue = predValues.get(predName.replace("!",""));
	    
	    predecessorsValues.add(predName.contains("!") ? 1-predValue : predValue);
	    if (predName.contains("!"))
		sum+= (1- predValue) * 2.2;
	    else
		sum+= predValue * 2.2; 
	}
	
	for (int i = 0; i<5-args.length; i++) //add '1' literals to complete to 3 literals
	{
	    sum+=2.2;
	}
	
	//return Sigmoid.SimulateAndGate(predecessorsValues, 8);
	
	return Sigmoid.Calculate(sum,10);
    }

    private boolean GetIsHasNeuralPredecessor()
    {
	for (PNode node : this.GetPredecessorsList())
	{
	    if (node.GetValue() != 0 && node.GetValue() != 1)
		return true;
	}
	return false;
    }

    // calculate node's value using nerual calculation
    public double CalculateAsNeural()
    {
	// ProgramSettings.neuralCalculations++;
	double predecessorsWeightedSum = this.GetWeightedSumOfPredecessors();
	this.SetValue(Sigmoid.Calculate(predecessorsWeightedSum + ps.GetConstantErrorDeltaFactor()));
	ProgramSettings.neuralCalculations++;
	return this.value;
    }

    // calcualte node's value using it's boolean function
    public double CalculateAsBoolean()
    {
	// ProgramSettings.booleanCalculations++;
	HashMap<String, Integer> predValues = new HashMap<>();
	for (PNode pred : GetPredecessorsList())
	{
	    predValues.put(pred.GetName(), pred.GetBooleanValue());
	}

	for (String e : predExpressions)
	{

	    if (GetBooleanFunctionValue(e, predValues) == 1)
	    {
		this.value = 1;
		this.isCalculated = true;
		return this.value;
	    }
	}
	this.value = 0;
	ProgramSettings.booleanCalculations++;
	return this.value;

    }

    // convert current value to boolean using the defined treshold
    public int GetBooleanValue()
    {
	if (this.value < ps.NeuralToBooleanThreshold())
	    return 0;
	else
	    return 1;
    }

    private int GetBooleanFunctionValue(String e, HashMap<String, Integer> predValues)
    {
	if (e.isEmpty())
	    return 0;

	String[] args = e.split("\\+|\\*");
	for (String arg : args)
	{
	    double value;
	    if (arg.contains("!"))
		value = 1 - predValues.get(arg.replace("!", ""));
	    else
		value = predValues.get(arg);

	    if (value == 0)
		return 0;
	}
	return 1;

    }

    public boolean GetIsCalculated()
    {
	return isCalculated;
    }

    public void SetDefaultValue(double value)
    {
	if (value == 1.0)
	    this.defaultValue = ProgramSettings.GetRandomNumberFromList(Network.randomValues);
	this.defaultValue = value;
    }

    public double GetDefaultValue()
    {
	return defaultValue;
    }

    public void SetIsCalculated(boolean val)
    {

	this.isCalculated = val;
    }

    // add predecessor to node
    public void AddPredecessor(PNode parentNode, double weight)
    {
	predecessors.put(parentNode, weight); // add new node to predecessors
					      // map.
    }

    public void AddPredecessor(PNode parentNode)
    {
	this.AddPredecessor(parentNode, Network.Default_Weight);
    }

    // add a descendant to this node's descendants list
    // adds this node to the descandent's predecessors list
    public void AddDescendant(PNode descandant)
    {
	this.descendants.add(descandant);
    }

    public double GetLastError()
    {
	return this.lastError;
    }

    public void SetLastError(double error)
    {
	this.lastError = error;
    }

    public void SetIsOutput(boolean value)
    {
	isOutput = value;

    }

    public boolean GetIsOutput()
    {
	return isOutput;
    }

    public double GetExperimentOutputValue()
    {
	return outputValueOfExperiment;
    }

    public boolean GetIsHidden()
    {
	return isHidden;
    }

    public void SetIsHidden(boolean value)
    {
	isHidden = value;
    }

    public double GetWeightedSumOfPredecessors()
    {
	double sum = 0;

	for (PNode pred : predecessors.keySet())
	{
	    
	    sum += pred.GetValue() * predecessors.get(pred);
	    
	}
	/*
	 * for (int i = 0; i < predecessors.size(); i++) { PNode node =
	 * this.GetPredecessorsList().get(i); sum += (double) node.GetValue() *
	 * predecessors.get(node); }
	 */
	return sum;
    }

    public void SetExperimentOutputValue(Double nodeValue)
    {
	outputValueOfExperiment = nodeValue;
    }

    public void UpdateEdgeWeightFrom(PNode parent, double newWeight)
    {

	if (!predecessors.containsKey(parent))
	{
	    System.out.println("There is no edge between " + parent.GetName() + " to " + this.GetName());
	    return;
	}

	predecessors.put(parent, newWeight); // updating weight

    }

    public void SetIsUnknownFunction(boolean value)
    {
	isUnknownFunction = value;

    }

    public boolean GetIsUnknownFunction()
    {
	return isUnknownFunction;
    }

    public void AddExpression(String expression)
    {
	this.predExpressions.add(expression);
    }

    public void SetPartialDerivative(double partialDerivative)
    {
	this.partialDerivarive = partialDerivative;
    }

    public Double GetPartialDerivarive()
    {
	return partialDerivarive;
    }

    public Double GetSumOfPredecessors()
    {
	if (predecessorList == null)
	    return 0.0;
	double sum = 0;
	for (PNode node : predecessorList)
	{
	    sum += node.GetValue();
	}
	return sum;

    }
    
    /*public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(name).
            append(value).
            toHashCode();
    } */
    
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof PNode))
            return false;

        PNode rhs = (PNode) obj;
        return this.name == rhs.name;
    }
    
    public ArrayList<String> GetTerms()
    {
	return predExpressions;
    }
   

}