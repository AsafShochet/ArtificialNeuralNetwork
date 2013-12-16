package Project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.time.StopWatch;
import ResultsAnalysis.ResultsAnalysis;
import ResultsAnalysis.LearningNetworkResult;

public class Main
{
    static String basePath, reacFileName, specFileName, expFileName, graphFolder, resultFolder;
   
    public static void main(String[] args)
    {
	if (args.length != 1)
	{
	    System.out.println("The program expects a single argument - the folder of the data files.\n" +
	    		"please supply the folder name itself, without additional slashes (e.g. c:\\myProject\\dataFiles)." +
	    		"\nTerminating..");
	    System.exit(0);
	}
	
	// Set source files location
	ProgramSettings.SetBasePath(args[0] + "\\"); 
	basePath = ProgramSettings.basePath;
	reacFileName = ProgramSettings.reactionsFileName;
	specFileName = ProgramSettings.specificationsFileName;
	expFileName = ProgramSettings.experimentsFileName;
	graphFolder = "";
	resultFolder = ProgramSettings.resultsFolder;

	VerifyFilesAndFolders();
	
	MainProcedure();
    }

    public static void MainProcedure()
    {
	// Variables Declarations
	double bestError = Double.MAX_VALUE;
	double finalError = 0;
	PropagationSettings ps = new PropagationSettings();
	ResultsAnalysis.InitializeAnalysisFile(ProgramSettings.resultsFileName, "general");
	ResultsAnalysis.InitializeAnalysisFile("Details.txt", "details");

	// Load network and experiments to memory
	Network model = NetworkLogic.GetInstance().BuildNetworkFromFiles(basePath + reacFileName, basePath + specFileName);
	List<Experiment> experiments = NetworkLogic.GetInstance().ReadExperimentsFromFile(basePath + expFileName);

	// Add a dummy parent to each unknown function.
	if (ps.GetAddDummyParent())
	{
	    for (PNode node : model.GetUnknownFunctions())
	    {
		String dummyParentName = "dummy_parent_" + node.GetName();
		PNode dummyNode = new PNode(dummyParentName);
		dummyNode.SetValue(1.0);
		model.AddNode(dummyNode);
		model.Link(dummyParentName, node.GetName());
	    }
	}

	// creates xgmml file for the network
	if (ProgramSettings.writeGraphFile)
	{
	    graphFolder = ResultsAnalysis.CreateFolder(ResultsAnalysis.currentAnalysisFolder, "graphs");
	    ResultsAnalysis.CreateGraphFile(graphFolder + "beforeLearning", model);
	}

	for (int processIteration = 0; processIteration < PropagationSettings.fullProcessIterations; processIteration++)
	{
	    //reset previous run settings
	    model.ResetNetwork();
	    ps.Reset();
	    
	    // Main Logic - learn the network according to experimental data
	    ResultsAnalysis.AddToAnalysis("general", "Process started at " + ProgramSettings.timestamp + "\n");
	    StopWatch watch = new StopWatch();
	    watch.start();
	    List<LearningNetworkResult> results = RunLearningProcess(model, experiments, ps);
	    watch.stop();
	    ResultsAnalysis.AddToAnalysis("general",String.format("Total calculations made:\nboolean: %d, neural: %d, boolean simulation: %d", ProgramSettings.booleanCalculations,
		    ProgramSettings.neuralCalculations, ProgramSettings.booleansigmoidalCalculationsZero + ProgramSettings.booleansigmoidalCalculationsOne));
	    ResultsAnalysis.AddToAnalysis("general", "total calculative time: " + watch.toString());
	    System.out.print("\n");

	    //in CV there is more than one result.
	    double errorsSum = 0;
	    for (LearningNetworkResult l : results)
	    {
		errorsSum += l.bestError;
	    }
	    if (errorsSum < bestError)
	    {
		bestError = errorsSum;
	    }

	    finalError = CalculateFinalError(model, experiments);
	    ResultsAnalysis.AddToAnalysis("general",
		    String.format("total error of model predictions comparing to the experimental data (SSE): %f \n", finalError));
	    
	    ResultsAnalysis.AddToAnalysis("general",ResultsAnalysis.ErrorsPerExperiment(model, experiments));
	    ResultsAnalysis.AddToAnalysis("general", ResultsAnalysis.ErrorsPerNode(model,experiments));
	    
	}
	
	ResultsAnalysis.CloseAnalysisFile("general");
	ResultsAnalysis.CloseAnalysisFile("details");
	System.out.format("Final Result: %f\n",finalError);
	System.out.println("Analysis files written to " + ProgramSettings.resultsFolder + "\\" + ProgramSettings.currentRunResultsfolder);
    }

    // ---------------- Internal Methods -----------------

    private static void VerifyFilesAndFolders()
    {
	String errorMessage = null;
	
	File f = new File(basePath);
	if (!f.exists() || !f.isDirectory()) errorMessage =  "Directory : " + basePath + " is not a valid directory name.\n";
	else if (!(new File(basePath + reacFileName)).exists()) errorMessage =  "Cannot find the file " + reacFileName + " \n";
	else if (!(new File(basePath + specFileName)).exists()) errorMessage =  "Cannot find the file " + specFileName + " \n";
	else if (!(new File(basePath + expFileName)).exists()) errorMessage =  "Cannot find the file " + expFileName + " \n";
	else if (!(new File(resultFolder)).exists())
	{
	    File dir = new File(resultFolder);
	    Boolean created = dir.mkdir();
	    if (!created) errorMessage =  "Error creating results folder" + resultFolder + ".\n";
	}
	
	if (errorMessage!= null)
	{
	    System.out.println(errorMessage + "Terminating..");
	    System.exit(0);
	}
    }

    // Run the main learning process
    public static List<LearningNetworkResult> RunLearningProcess(Network model, List<Experiment> experiments, PropagationSettings ps)
    {
	List<LearningNetworkResult> resultsList = new ArrayList<>();
	double currentError = 0;
	double totalError = 0;
	int numOfIterations = 0;

	if (ps.GetCalculationMethod() == "Boolean") // do not learn - just use
						    // boolean functions as they
						    // are
	{
	    ResultsAnalysis.AddToAnalysis("general", "Calculation Method: Boolean");
	    System.out.print("Learning");
	    for (Experiment exp : experiments)
	    {
		totalError += CalculateError(model, exp);
	    }
	    resultsList.add(new LearningNetworkResult(model, totalError));

	}

	else if (!ps.UseCV()) // simple learning logic
	{
	    ResultsAnalysis.AddToAnalysis("general", "Calculation Method: Neural/Mixed");
	    System.out.print("Learning");
	    while (!ps.ShouldStopPropagate())
	    {
		for (Experiment exp : experiments)
		{
		    model.Learn(exp, ps);
		    numOfIterations++;
		}

		if (numOfIterations % 800 > 0 && numOfIterations % 800 < 30)
		{
		    System.out.print(".");
		}

		currentError = CalculateError(model, experiments);
		ps.Update(currentError, model, numOfIterations);
	    }

	    totalError = CalculateError(model, experiments);
	    LearningNetworkResult learningNetworkResults = new LearningNetworkResult(model, totalError);
	    resultsList.add(learningNetworkResults);

	}

	else
	// learn using Cross Validation
	{
	    ResultsAnalysis.AddToAnalysis("general", "Calculation Method: Neural/Mixed With Cross Validation");
	    System.out.print("Learning");
	    double validationDataError = 0;
	    List<List<Experiment>> CVLists = CreateCVExperimentsLists(experiments, ps.GetNumOfCVSubsets());

	    for (List<Experiment> validationData : CVLists)
	    {
		experiments.removeAll(validationData);

		while (!ps.ShouldStopPropagate())
		{
		    for (Experiment exp : experiments)
		    {
			model.Learn(exp, ps);
			numOfIterations++;
		    }

		    if (numOfIterations % 800 > 0 && numOfIterations % 800 < 30)
		    {
			System.out.print(".");
		    }

		    currentError = CalculateError(model, experiments);
		    ps.Update(currentError, model, numOfIterations);
		}

		ResultsAnalysis.AddToAnalysis("general", String.format("\n__________Num of iterations is %d____________", numOfIterations));

		double partitionError = CalculateError(model, validationData);
		validationDataError += partitionError;

		ResultsAnalysis.AddToAnalysis("general", String.format("\nValidation data error is %fl\n", validationDataError));

		LearningNetworkResult learningNetworkResults = new LearningNetworkResult(model, partitionError);
		resultsList.add(learningNetworkResults);

		experiments.addAll(validationData);
		ps.Reset();
		numOfIterations = 0;
		partitionError = 0;
	    }
	}
	return resultsList;

    }

    // Used for CV calculation. returns a list of lists of experiments.
    private static List<List<Experiment>> CreateCVExperimentsLists(List<Experiment> experiments, int numberOfGroups)
    {
	List<List<Experiment>> result = new ArrayList<>();
	List<Experiment> expList;

	Collections.shuffle(experiments); // shuffles the order of the
					  // experiments.

	int groupSize = experiments.size() / numberOfGroups;
	if (experiments.size() % numberOfGroups != 0)
	    groupSize++;

	ResultsAnalysis.AddToAnalysis("general", "The validation experiments are:\n");

	int index = 0;
	for (int i = 0; i < numberOfGroups; i++)
	{
	    // Log(String.format("\ngroup %d:\n", i));
	    expList = new ArrayList<>();
	    for (int j = 0; j < groupSize; j++)
	    {
		index = j * numberOfGroups + i;
		if (index < experiments.size())
		{
		    expList.add(experiments.get(index));
		}

	    }
	    result.add(expList);
	}

	return result;
    }

    // calculates experiment error for specific experiments on a specific
    // weighted network

    private static double CalculateError(Network network, List<Experiment> experimentsList)
    {
	double totalError = 0;
	for (Experiment exp : experimentsList)
	{
	    totalError += exp.GetTotalError(network); // calculate error
	}
	return totalError;
    }

    // calculate the error of the model predictions for a single experiment
    private static double CalculateError(Network network, Experiment e)
    {
	double totalError = 0;
	List<Experiment> l = new ArrayList<Experiment>();
	l.add(e);
	for (Experiment exp : l)
	{
	    totalError += exp.GetTotalError(network); // calculate error
	}
	return totalError;

    }

    //calculate the SSE of the model comparing to the experimental data
    private static double CalculateFinalError(Network network, List<Experiment> experiments)
    {
	double totalError = 0;
	double expError = 0;
	int expIndex = 1;
	ResultsAnalysis.AddToAnalysis("details","Detailed results per experiment (in the brackets - the SSE):\n");
	for (Experiment exp : experiments)
	{
	    ResultsAnalysis.AddToAnalysis("details",String.format("experiment no %d:",expIndex));
	    network.FeedForward(exp);
	    for (String nodeName : exp.GetOutputs().keySet())
	    {

		double calculated = network.GetNode(nodeName).GetValue();
		double experimental = exp.outputs.get(nodeName);
		double sse = Math.pow(calculated - experimental, 2);
		expError += sse;
		ResultsAnalysis.AddToAnalysis("details", String.format("node: %s calculated value: %f experimental value: %f (%f)", 
			nodeName,calculated,experimental,sse));
		
	    }
	    totalError += expError;
	    expError = 0;
	    expIndex++;
	    ResultsAnalysis.AddToAnalysis("details", " ");
	}
	

	return totalError;
    }
}