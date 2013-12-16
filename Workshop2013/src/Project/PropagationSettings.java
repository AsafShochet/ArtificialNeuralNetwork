package Project;

import ResultsAnalysis.ResultsAnalysis;

//This class include all settings that define the learning process and the backpropogation algorithm behavior

public class PropagationSettings
{
    //Cross Validation settings. CV stands for Cross Validation  
    private int CVSubsets = 5;
    private boolean useCV = false; // use Cross Validation in calculations
    
    //calculation Method
    private String calculationMethod = "Mixed";
    //private String calculationMethod = "Neural";
    //private String calculationMethod = "Boolean";
    
    //number of iterations over the whole learning process. used to produced average results
    public static int fullProcessIterations = 1;
 
    //boolean to neural conversion settings - not necessarily used
    private static double neuralToBooleanThreshold = 0.4; 
    private boolean replaceOutputsWithFraction = false;
    private static boolean replaceInputsAndDefaultsWithFraction = false;
    public static double zeroVal = 0.05;
    public static double oneVal = 0.95;
    
    
    //general learning settings
    private double learningRate = 0.5;
    private double constantErrorDeltaFactor = 0;
    private boolean learnOnlyUnknownFunctions = true; // learn only the unknown functions
    private boolean convertToBooleanOnErrorCalculation = false;
    private int maxWeightGuesses = 1; // how many times to run the whole process with different initial weights - currently not used
    private boolean addDummyParent = true; //add/don't add a dummy parent valued 1 to each unknown function
    
    
    //-------- learning algorithm stopping conditions ------
    
    //stop when there is no real change in weights for a number of iterations
    public double WeightsDiffThreshold = 0.0000001; 
    public int MaxConsecutiveSmallWeightsDiff = 100;  
    
    //stop when there is no real improvement in model predictions comparing to the experiments outputs for a number of iterations
    public double SmallErrorThreshold = 0.01;
    public int MaxConsecutiveSmallErrorDiff = 1;
   
    //stop when reaching this number of iterations
    public int MaxNumberOfIterations = 10000000;
    
    //stop when the total difference from the experiments outputs is good enough - not really a factor
    public double AcceptableErrorMargin = 0.0000001;
    
   //variables used to assess the above
    public int ConsecutiveSmallErrorDiff = 0;
    public int numberOfIterations = 0;
    public int ConsecutiveSmallWeightsDiff = 0;
    private double lastGlobalError = Double.MAX_VALUE;
    private double lastWeightsSum;
   
    //check if the learning process should stop
    public boolean ShouldStopPropagate()
    {
	boolean stop = false;
	String reason = null;
	if (ConsecutiveSmallWeightsDiff >= MaxConsecutiveSmallWeightsDiff) //no change in weights for many iterations
	{
	    reason = "ConsecutiveSmallWeightsDiff >= MaxConsecutiveSmallWeightsDiff"; 
	    stop =  true;
	}
	else if (ConsecutiveSmallErrorDiff >= MaxConsecutiveSmallErrorDiff) //no improvements in the total error compared to experiments
	{
	    reason = "ConsecutiveSmallErrorDiff >= MaxConsecutiveSmallErrorDiff";
	    stop =  true;
	}
	else if (numberOfIterations >= MaxNumberOfIterations) //reached maximum number of iterations allowed
	{
	    reason = "numberOfIterations >= MaxNumberOfIterations";
	    stop =  true;
	}
	else if (this.lastGlobalError < AcceptableErrorMargin) //the result is good enough
	{
	    reason = "lastGlobalError < AcceptableErrorMargin";
	    stop = true;
	}
	
	if (stop)
	{
	    System.out.println();
	    ResultsAnalysis.AddToAnalysis("general", "The reason the training process stopped:\n" + reason);
	}
	
	return stop;
    }

    //update local variables with the current status of the network.
    public void Update(double globalError, Network model, int numberOfIterations)
    {
	if (Math.abs(globalError - this.lastGlobalError) < SmallErrorThreshold)
	    ConsecutiveSmallErrorDiff++;
	if (Math.abs(model.GetSumOfWeights() - lastWeightsSum) < WeightsDiffThreshold)
	    ConsecutiveSmallWeightsDiff++;

	this.numberOfIterations = numberOfIterations;
	this.lastGlobalError = globalError;
	this.lastWeightsSum = model.GetSumOfWeights();
    }

    public double GetLearningRate()
    {
	return learningRate;
    }

    public double NeuralToBooleanThreshold()
    {
	return neuralToBooleanThreshold;
    }

    public double GetConstantErrorDeltaFactor()
    {
	return constantErrorDeltaFactor;
    }

    public boolean ReplaceOutputsWithFraction()
    {
	return replaceOutputsWithFraction;
    }

    public static double GetFractionValue(double value)
    {
	return value == 0.0 ? zeroVal : oneVal;
    }
    
    public static int GetBooleanValue(double value)
    {
	return value > neuralToBooleanThreshold ? 1 : 0;
    }

    public boolean UseCV()
    {

	return useCV;
    }

    public boolean LearnOnlyUnknownFunctions()
    {
	return learnOnlyUnknownFunctions;
    }

    public String GetCalculationMethod()
    {
	return calculationMethod;
    }

    public void SetCalculationMethod(String calculationMethod)
    {
	this.calculationMethod = calculationMethod;
    }

    public int MaxWeightGuesses()
    {
	return maxWeightGuesses;
    }

    public int GetNumOfCVSubsets()
    {
	return CVSubsets;
    }

    public static boolean ReplaceInputsAndDefaultsWithFraction()
    {
	return replaceInputsAndDefaultsWithFraction;
    }

    public boolean ConvertToBooleanOnErrorCalculation()
    {
	return convertToBooleanOnErrorCalculation;
    }
    
    public boolean GetAddDummyParent()
    {
	return addDummyParent;
    }
    
    //reset the counters
    public void Reset()
    {
	ConsecutiveSmallWeightsDiff = 0;
	ConsecutiveSmallErrorDiff = 0;
	numberOfIterations = 0;
	lastGlobalError = Double.MAX_VALUE;
    }
}
