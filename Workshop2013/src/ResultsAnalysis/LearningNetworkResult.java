package ResultsAnalysis;

import java.util.ArrayList;
import java.util.List;
import Project.Network;

public class LearningNetworkResult
{
    public Network bestModel;
    public double bestError;
    public List<Integer> measuredExperimentNumbers = new ArrayList<>();

    public LearningNetworkResult(Network bestModel, double bestError)
    {
	this.bestError = bestError;
	this.bestModel = bestModel;
    }
}
