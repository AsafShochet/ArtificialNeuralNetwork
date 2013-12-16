package ResultsAnalysis;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import Project.Edge;
import Project.Network;
import Project.PNode;

public class XgmmlHandler
{

    public static void CreateGraphFile(String filename, Network network)
    {

	String basepath = "c:\\biologicalNetwork\\xgmml\\";
	String headerFile = "xgmmlHeader.txt";
	String edgeFile = "xgmmlEdge.txt";
	String vertexFile = "xgmmlVertex.txt";
	String footerFile = "xgmmlFooter.txt";
	if (!filename.endsWith(".xgmml")) // asserting file ends with .xgmml -
					  // graph extension
	    filename += ".xgmml";

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
	    System.err
		    .format("security exception when creating\\deleting file named %s"
			    + filename);
	}
	catch (IOException x)
	{
	    // Some other sort of failure, such as permissions.
	    System.err.format("createFile error: %s%n", x);
	}

	// header
	String headerText = GetTextFromFile(basepath + headerFile);
	headerText = headerText.replaceAll("FILE_NAME", filename);

	PrintWriter writer = null;

	try
	{
	    writer = new PrintWriter(filename, "UTF-8");
	}
	catch (Exception e)
	{
	    System.out.println("error making a new PrintWriter\n"
		    + e.toString());
	}
	writer.print(headerText);

	for (PNode v : network.TopologicalSort())
	{
	    String nodeText = GetTextFromFile(basepath + vertexFile);
	    nodeText = nodeText.replaceAll("NODE_ID", v.hashCode() + "");
	    nodeText = nodeText.replaceAll("NODE_NAME", v.GetName());
	    writer.print(nodeText);

	}

	for (Edge e : network.Edges())
	{
	    PNode source = e.source;
	    PNode dest = e.destination;
	    String edgeText = GetTextFromFile(basepath + edgeFile);
	    edgeText = edgeText.replaceAll("NODE_ID_1", source.hashCode() + "");
	    edgeText = edgeText.replaceAll("NODE_ID_2", dest.hashCode() + "");
	    edgeText = edgeText.replaceAll("NODE_NAME_1", source.GetName());
	    edgeText = edgeText.replaceAll("NODE_NAME_2", dest.GetName());
	    edgeText = edgeText.replaceAll("EDGE_ID",
		    (source.hashCode() + dest.hashCode()) + "");
	    edgeText = edgeText.replaceAll("EDGE_WEIGHT", e.GetWeight()
		    .toString());
	    writer.print(edgeText);
	}

	String footerText = GetTextFromFile(basepath + footerFile);
	writer.print(footerText);
	writer.close();
    }

    private static String GetTextFromFile(String filename)
    {
	try
	{
	    String everything = "";
	    BufferedReader br = new BufferedReader(new FileReader(filename));
	    try
	    {
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();

		while (line != null)
		{
		    sb.append(line);
		    sb.append("\n");
		    line = br.readLine();
		}
		everything = sb.toString();
	    }
	    finally
	    {
		br.close();

	    }

	    return everything;
	}
	catch (Exception e)
	{
	    System.out.println("Error reading file: " + filename);
	    return null;
	}

    }
}
