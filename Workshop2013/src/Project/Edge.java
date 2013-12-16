package Project;

public class Edge {

	public PNode source;
	public PNode destination;
	public Double weight;
	private String label;
	public Edge(PNode source, PNode destination, Double weight) {

		this.source = source;
		this.destination = destination;
		this.weight = weight;
		this.label = source.GetName()+":"+destination.GetName();
	}
	
	public Double GetWeight()
	{
		return this.weight;
	}
	
	public String Label()
	{
		return this.label;
	}

}
