package CVRP.columnGeneration;

import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import optimisation.columnGeneration.AbstractColumn;

public abstract class CVRPColumn extends AbstractColumn<CVRPInstance, CVRPPricingProblem>
{
	public CVRPColumn(double coefficient)
	{
		super(coefficient, false);
	}
}
