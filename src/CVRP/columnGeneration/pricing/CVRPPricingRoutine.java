package CVRP.columnGeneration.pricing;

import java.util.ArrayList;
import java.util.List;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.instance.CVRPConstants;
import CVRP.instance.CVRPInstance;
import CVRP.instance.CustomerNode;
import graph.structures.digraph.DirectedGraphArc;
import optimisation.columnGeneration.pricing.AbstractPricingRoutine;

public class CVRPPricingRoutine
		extends AbstractPricingRoutine<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	@Override
	protected void preProcessPricingProblems(CVRPInstance instance)
	{
		// Do nothing.
	}

	@Override
	protected void preProcessPricingProblem(CVRPInstance instance, CVRPPricingProblem pricingProblem) 
	{
		// Reset duals.
		for (DirectedGraphArc<CustomerNode, Integer> arc : pricingProblem.getGraph().getArcs())
		{
			arc.setDual(0, 0);
			arc.setDual(0, 1);
		}
	}

	@Override
	protected List<CVRPPricingProblem> generatePricingProblems(CVRPInstance instance)
	{
		List<CVRPPricingProblem> pricingProblems = new ArrayList<>();

		switch (CVRPConstants.FORMULATION)
		{
			case "VEHICLE_INDEX":
				// A single pricing problem per vehicle.
				for (int k = 0; k < instance.getK(); k++)
				{
					CVRPPricingProblem pp = new CVRPPricingProblem(k, instance);
					pricingProblems.add(pp);
				}
				break;
			case "LAST_CUSTOMER":
				// A single pricing problem per potential last customer.
				for (int i = 1; i <= instance.getN(); i++)
				{
					CVRPPricingProblem pp = new CVRPPricingProblem(0, instance);
					pp.setCustomer(i);
					pricingProblems.add(pp);
				}
				break;
			case "NO_CONSTRAINTS":
				// One single pricing problem.
				CVRPPricingProblem pp = new CVRPPricingProblem(0, instance);
				pricingProblems.add(pp);
				break;
			default:
				break;
		}
		return pricingProblems;
	}
}
