package CVRP.columnGeneration.branching;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.RouteColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import optimisation.BAP.AbstractBranchingRule;
import optimisation.BAP.BAPNode;
import optimisation.BAP.BranchingCandidate;
import util.Configuration;

public class BranchingRuleLastCustomer extends AbstractBranchingRule<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	public BranchingRuleLastCustomer(double priority)
	{
		super(priority);
	}

	@Override
	public List<BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem>> getBranchingCandidates(
			BAPNode<CVRPInstance, CVRPColumn, CVRPPricingProblem> parent)
	{
		// Compute value per customer.
		Map<Integer, Double> customerValueMap = new LinkedHashMap<>();

		for (Entry<CVRPColumn, Double> entry : parent.getSolution().getColumnMap().entrySet())
		{
			if (!(entry.getKey() instanceof RouteColumn))
			{
				continue;
			}
			RouteColumn routeColumn = (RouteColumn) entry.getKey();
			double value = entry.getValue();
			List<Integer> nodes = routeColumn.getRoute().getNodes();
			int customer = nodes.get(nodes.size() - 2);
			if (!customerValueMap.containsKey(customer))
			{
				customerValueMap.put(customer, 0.0);
			}
			customerValueMap.put(customer, customerValueMap.get(customer) + value);
		}

		// Generate branching candidates.
		List<BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem>> candidates = new ArrayList<>();
		double branchingPrecision = Configuration.getConfiguration().getDoubleProperty("BRANCHING_PRECISION");
		for (Entry<Integer, Double> entry : customerValueMap.entrySet())
		{
			double value = entry.getValue();
			double fractionalValue = Math.abs(value - Math.rint(value));
			if (fractionalValue > branchingPrecision)
			{
				BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem> candidate = new BranchingCandidate<>(
						fractionalValue);
				candidate.addBranchingDecision(new BranchingDecisionLastCustomer(true, entry.getKey()));
				candidate.addBranchingDecision(new BranchingDecisionLastCustomer(false, entry.getKey()));
				candidates.add(candidate);
			}
		}
		return candidates;
	}
}
