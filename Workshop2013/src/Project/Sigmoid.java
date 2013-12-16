package Project;

import java.util.List;

public class Sigmoid
{

	public static double Calculate(double x)
	{
	    return 1 / (1 + Math.exp(-1 * x));
	    //return 1/(1+Math.exp(-8*x + 4));
	}
	
	public static double Calculate(double x, double constant)
	{
	    return 1 / (1 + Math.exp(-1 * x + constant));
	    //return 1/(1+Math.exp(-8*x + constant));
	}
	
	public double Derivative(double x)
	{
	    return (1/(1+ Math.exp(-1 * x)) * (1 - 1/(1+ Math.exp(-1 * x))));
	    //return (8*Math.exp(4-8*x)) / (Math.pow(Math.exp(4-8*x) + 1,2));
	}
	
	public static double SimulateAndGate(List<Double> values, int factor)
	{
	    double constant = -factor *(values.size() - 0.5);
	    double sum = constant;
	    for (double val : values)
	    {
		sum+=factor*val;
	    }
	
	    return 1/(1+Math.exp(-sum));
	}

	public static double SimulateOrGate(List<Double> values, int factor)
	{
	    double constant = -factor *0.5;
	    double sum = constant;
	    for (double val : values)
	    {
		sum+=factor*val;
	    }
	
	    return 1/(1+Math.exp(-sum));
	}	
}
