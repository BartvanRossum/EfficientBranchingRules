package GAP.columnGeneration.pricing;

import java.util.ArrayList;
import java.util.List;

import GAP.GAPInstance;
import GAP.columnGeneration.GAPColumn;
import optimisation.columnGeneration.pricing.AbstractPricingRoutine;

public class GAPPricingRoutine extends AbstractPricingRoutine<GAPInstance, GAPColumn, GAPPricingProblem>
{
	@Override
	protected void preProcessPricingProblems(GAPInstance instance)
	{
		// Do nothing.
	}

	@Override
	protected void preProcessPricingProblem(GAPInstance instance, GAPPricingProblem pricingProblem)
	{
		pricingProblem.resetDuals();
	}

	@Override
	protected List<GAPPricingProblem> generatePricingProblems(GAPInstance instance)
	{
		List<GAPPricingProblem> pricingProblems = new ArrayList<>();
		for (int i = 0; i < instance.getNumAgents(); i++)
		{
			pricingProblems.add(new GAPPricingProblem(instance, i));
		}
		return pricingProblems;
	}
}
