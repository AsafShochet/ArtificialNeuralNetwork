import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PNode {
	private String name;
	private HashSet<PNode> predecessors;
	private HashSet<PNode> descendants;
	private boolean isFixedByExperiment;
	private double value;
	private boolean isProcessed = false;
	private byte defaultState = -1;
	private List<String> ANDExpressionsWithORbetween; // & expressions with ||
														// between them
	private List<String> predExpressions = new ArrayList<String>();

	public String GetName() {
		return name;
	}

	public void SetDefaultState(byte value) {
		this.defaultState = value;
	}

	public byte GetDefaultState() {
		return this.defaultState;
	}

	public PNode(String name) {
		this.name = name;
		this.descendants = new HashSet<PNode>();
		this.predecessors = new HashSet<PNode>();
		this.ANDExpressionsWithORbetween = new ArrayList<>();
	}

	// returns predecessors list
	public HashSet<PNode> GetPredecessors() {
		return predecessors;
	}

	// returns value
	public double GetValue() {
		return value;
	}

	// sets value
	public void SetValue(double newValue) {
		value = newValue;

	}

	// reset fields
	public void ResetValue() {
		this.SetValue(0);
		this.SetIsProcessed(false);
		this.SetIsFixed(false);
	}

	// returns IsFixedByExperiment
	public Boolean GetIsFixed() {
		return isFixedByExperiment;
	}

	public void SetIsFixed(boolean value) {
		isFixedByExperiment = value;

	}

	public HashSet<PNode> GetChildren() {
		return descendants;
		// returns children list
	}

	// pre: all predecessors are set, and BoolFunction is set, post:
	// node’s
	// value is set based on function and predecessors.

	public double Calculate() {

		if (isProcessed)
			return value;

		else if (predExpressions.size() == 0)
		{
			value = defaultState;
			return value;
			
		}
		

			// String expression = this.expression;
			HashMap<String, Double> predValues = new HashMap<>();
			for (PNode pred : predecessors) {
				predValues.put(pred.GetName(), pred.Calculate());
				// double val = pred.Calculate(); // if it's true
				// ->
				// 1, else: 0;
				// expression = expression.replace("$" + pred.GetName() + "$",
				// val+"");
			}

			// String[] miniExps = expression.split("||");

			for (String e : predExpressions) {
				if (CalculateBoolAndExpression(e, predValues) == 1)
				{
					this.value = 1;
					this.isProcessed = true;
					return this.value;
				}
			}
			this.value = 0;
			this.isProcessed = true;
			return this.value;
		
		}
		/**
		 * expression = expression.replace("+", "&&"); // replacing + with //
		 * logical // and && expression = expression.replace("$", "");
		 * 
		 * 
		 * this.value = CalculateORExpression(expression);
		 * 
		 * 
		 * }
		 **/
	
		
	private int CalculateBoolAndExpression(String e,
			HashMap<String, Double> predValues) {

		if (e.isEmpty())
			return 0;
		String[] args = e.split("\\+");
		for (String arg : args) {
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

	// add predecessor to the list
	public void AddPredecessor(PNode parentNode) {
		predecessors.add(parentNode); // add new node to predecessors, and add
		// this node to it's children
	}

	// add a child to this node's children list
	// adds this node to the child's node list
	public void AddChild(PNode childNode) {
		this.descendants.add(childNode);
		childNode.AddPredecessor(this);
	}

	// adds the expression string to the existing one.
	// example:
	// this.expression = (!$j$)
	// expression = x+y
	// then, after running this method:
	// this.expression == (!$j$)||($x$+$y$)
	// we put every protein name between $name$
	public void AddExpression(String expression) {
		this.predExpressions.add(expression);
		/**
		 * expression = expression.replace("!", "!$").replace("+!", "$+!$")
		 * .replace("+", "$+$"); if (this.predExpressions.isEmpty())
		 * this.expression = "($" + expression + "$)";
		 * 
		 * else this.expression += "##($" + expression + "$)";
		 * 
		 * this.expression = this.expression.replace("$$", "$");
		 **/
	}

	public PNode GetNextUnvisitedChild() {

		for (PNode node : descendants) {
			// System.out.println("unvisited: " + node.GetName());
			if (!node.IsProcessed())
				return node;
		}
		return null;

	}

	public boolean IsProcessed() {
		return isProcessed;
	}

	public void SetIsProcessed(boolean value) {
		isProcessed = value;
	}

}