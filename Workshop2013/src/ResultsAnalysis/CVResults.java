package ResultsAnalysis;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Project.Edge;
import Project.PropagationSettings;

public class CVResults {

	HashMap<Edge, List<Double>> edgesWeights;
	List<LearningNetworkResult> networksResults = new ArrayList<>();
	double bestTotalError;
	String directoryPath;

	public CVResults(List<LearningNetworkResult> bestResults,
			double bestError, String directoryPath) {

		this.bestTotalError = bestError;
		this.networksResults = bestResults;
		this.directoryPath = directoryPath;
		this.edgesWeights = GetEdgesWeights();
	}

	public double Average(List<Double> group) {
		double sum = 0;
		for (double elem : group)
			sum += elem;
		return sum / group.size();
	}

	public double StandardDeviation(List<Double> group) {
		double average = Average(group);
		double sum = 0;
		for (double elem : group)
			sum += Math.pow((elem - average), 2);
		double sqrSum = Math.sqrt(sum);
		return sqrSum / group.size();
	}

	private HashMap<Edge, List<Double>> GetEdgesWeights() {
		HashMap<Edge, List<Double>> edgesWeights = new HashMap<>();

		for (LearningNetworkResult net : networksResults)
			for (Edge edge : net.bestModel.Edges()) {
				boolean found = false;
				for (Edge edgeInList : edgesWeights.keySet())
				{
					if (edgeInList.Label().equals(edge.Label())) //edge is found on the list, then we'll add another weight for it
					{
						edgesWeights.get(edgeInList).add(edge.GetWeight());
						found = true;
					}
				}
				if (found == false) //we didn't found this edge in the list
				{
					edgesWeights.put(edge, new ArrayList<Double>());
					edgesWeights.get(edge).add(edge.GetWeight());
					
				}
			}
				

		return edgesWeights;

	}

	// building an XML for the network results
	public void CreateLearningResultsXml(String filename) {

		PropagationSettings ps = new PropagationSettings();
		File f = new File(filename);

		try {
			// Create the empty file

			if (f.exists())
				f.delete();

			f.createNewFile();

		} catch (SecurityException e) {
			System.err
					.format("security exception when creating\\deleting file named %s"
							+ filename);
		} catch (IOException x) {
			// Some other sort of failure, such as permissions.
			System.err.format("createFile error: %s%n", x);
		}
		PrintWriter writer = null;

		try {
			writer = new PrintWriter(filename, "UTF-8");
		} catch (Exception e) {
			System.out.println("error making a new PrintWriter\n"
					+ e.toString());
		}

		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		writer.print("<LearningNetworkResults>");
		writer.print("<TotalError>" + this.bestTotalError + "</TotalError>");
		writer.print("<Method>" + ps.GetCalculationMethod() + "</Method>");
		writer.print("<Configuraions>");
		writer.print("<UseCrossValidation>" + ps.UseCV()
				+ "</UseCrossValidation>");
		writer.print("<LearningRate>" + ps.GetLearningRate()
				+ "</LearningRate>");
		writer.print("<LearnOnlySpecialNodes>");
		writer.print(ps.LearnOnlyUnknownFunctions());
		writer.print("</LearnOnlySpecialNodes>");
		writer.print("</Configuraions>");
		
		writer.print("<Edges>");

		for (Edge e : edgesWeights.keySet()) {
			writer.print("<Edge>");

			writer.print("<Lable>");
			String source = e.source.GetName();
			String dest = e.destination.GetName();
			writer.print(String.format("%s:%s", source, dest)); // edge a->b
																// with weight
																// 5.34, will be
																// a line with
																// text:
																// "a:b=5.34"
																// (without the
																// quoutes)
			writer.print("</Lable>");
			writer.print("<Source>");
			writer.print(source);
			writer.print("</Source>");
			writer.print("<Destination>");
			writer.print(dest);
			writer.print("</Destination>");
			
			writer.print("<Weight>");
			writer.print(Average(edgesWeights.get(e)));
			writer.print("</Weight>");
			writer.print("<StandardDeviation>");
			double stdDeviation = StandardDeviation(edgesWeights.get(e));
			writer.print(stdDeviation);
			writer.print("</StandardDeviation>");
			writer.print("</Edge>");
		}
		writer.print("</Edges>");
		writer.print("</LearningNetworkResults>"); // end of file
		writer.close();
		System.out.println("Created output XML file: "+filename);
		return;

	}
	
}
