package ResultsAnalysis;
import java.util.HashMap;

import Project.PNode;


public class PNodeExperimentResults {

	PNode pnode;
	HashMap<Integer, Double> errorsInExperiments = new HashMap<>();
	double totalDistanceFromOutput;
	double percentageOfHitsUnderEpsilon;
	
	public PNodeExperimentResults(PNode p)
	{
		this.pnode = p;
	}
	
	public void SetErrorOnExperiment(int expNumber, double error)
	{
		errorsInExperiments.put(expNumber,error);
	}
	
	public double GetAverageDistanceFromOutput()
	{
		
		int numberOfExp = errorsInExperiments.values().size();
		if (numberOfExp == 0)
			return 0.0;
		double total = 0;
	for (double e : errorsInExperiments.values())
		total+=e;
	
	double res = total / numberOfExp;
	return res;
	}
		
	
	public int GetNumberOfErrorsUnderEpsilon(double epsilon)
	{
		int errorsUnderEpsilon = 0;
		for (double e : errorsInExperiments.values())
			if (e < epsilon)
				errorsUnderEpsilon++;
		
		return errorsUnderEpsilon;
	}
}
