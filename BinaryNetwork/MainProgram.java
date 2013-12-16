import java.util.ArrayList;
import java.util.List;

public class MainProgram {

	/**
	 * public static void main(String [] args) { String reacPath = args[0];
	 * String specPath = args[1];
	 **/
	public static void main() {

		String basePath = "c:\\test\\";
		String reacFileName = "egfr_reac.txt";
		String specFileName = "egfr_spec.txt";
		String expFileName = "egfr_exp.txt";

		Network model = NetworkLogic.GetInstance().BuildNetworkFromFiles(
				basePath + reacFileName, basePath + specFileName);

		List<Experiment> experiments = NetworkLogic.GetInstance()
				.ReadExperimentsFromFile(basePath + expFileName);

		int totalResults = 0;
		int totalResultsAsExpected = 0;

		int i = 0;
		  for (Experiment exp : experiments) 
		  { String[] result =
		  exp.RunTest(model).split("\\|"); int match =
		  Integer.parseInt(result[0]); int total = Integer.parseInt(result[1]);
		  totalResults += total; // total outputs that // matched //
		  //predication 
		  totalResultsAsExpected += match;
		  System.out.println("experiment " + i + " simulation result: " + match
		  + " / " + total + "."); i++; }
		  for (Experiment exp : experiments) { i =
		  experiments.indexOf(exp); exp.RunTest(model); for (String pName :
		  exp.measuredNodes.keySet()) {
		  
		  double sValue, expValue; sValue = model.GetNode(pName).GetValue(); 
		  expValue = exp.measuredNodes.get(pName);
		  
		  System.out.println(i + " : " +pName+ " : " + sValue + " : " +
		  expValue); } }
		  System.out.println("Total: " + totalResultsAsExpected + "/"				+ totalResults + ".");

		 
		//BuildExperimentsWithSpecialNodes(model, experiments);
		
	}
	
	public static void BuildExperimentsWithSpecialNodes(Network model, List<Experiment> experiments)
	{
		int i;
		String[] specialNodes = { "sos1_e_e_dum", "vav2", "mekk1", "raf1",
				"mkk3", "mkk6", "mkk4", "mekk1", "mekk4", "mlk3", "pi34p2",
				"pip3", "pak1", "csrc", "p90rsk", "mk2", "akt", "pak1", "akt",
				"p90rsk", "mk2", "pten", "jnk", "erk12", "p90rsk", "erk12",
				"jnk", "csrc", "erbb11", "erbb11", "eps8r", "dag", "ca" };
		List<String> specialNodesList = new ArrayList<>();
		for (String p : specialNodes)
			// inputs
			if (!specialNodesList.contains(p)) {
				specialNodesList.add(p);
				System.out.println(p);
			}

		List<String> topSortNames = new ArrayList<String>();
		for (PNode p : model.TopologicalSorted())
			topSortNames.add(p.GetName());

		System.out.println("Model contains:");
		for (String p : specialNodesList) // inputs
		{
			if (topSortNames.contains(p))
				System.out.print(p + ",");
		}
		System.out.println();
		System.out.println("Model doesn't contain:");
		for (String p : specialNodesList)
			// inputs
			if (!topSortNames.contains(p))
				System.out.print(p + ",");

		System.out.println();
		for (Experiment exp : experiments) {
			i = experiments.indexOf(exp);
			exp.RunTest(model);

			for (PNode p : model.TopologicalSorted()) {
				String pName = p.GetName();
				if (exp.fixedNodes.keySet().contains(pName) || specialNodesList.contains(pName)) {
					double value;
					if (exp.fixedNodes.keySet().contains(pName))
					{
						value = exp.fixedNodes.get(pName);
						
						System.out.print(pName + " " +  + value + " ");
					}
					else if (specialNodesList.contains(pName) && model.GetNode(pName).IsProcessed())
					{
						value = model.GetNode(pName).GetValue();
						System.out.print(pName + " " + value 	+ " ");
					}
				}
			}
			System.out.print("| | ");

			for (String p : exp.measuredNodes.keySet()) {
				double value;
				value = exp.measuredNodes.get(p);
				System.out.print(p + " " + value + " ");
			}
			System.out.print("# #");

			System.out.println();
		}

		
	}

}
