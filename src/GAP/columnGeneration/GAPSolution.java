package GAP.columnGeneration;

import java.util.Map;

import GAP.GAPInstance;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import optimisation.columnGeneration.AbstractSolution;

public class GAPSolution extends AbstractSolution<GAPInstance, GAPColumn, GAPPricingProblem>
{
	public GAPSolution(double objectiveValue, Map<GAPColumn, Double> columnMap)
	{
		super(objectiveValue, columnMap);
	}
}
