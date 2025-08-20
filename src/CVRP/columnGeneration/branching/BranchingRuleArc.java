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

public class BranchingRuleArc extends AbstractBranchingRule<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	public BranchingRuleArc(double priority)
	{
		super(priority);
	}

	@Override
	public List<BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem>> getBranchingCandidates(
			BAPNode<CVRPInstance, CVRPColumn, CVRPPricingProblem> parent)
	{
		// Compute value per arc.
		Map<Arc, Double> arcValueMap = new LinkedHashMap<>();

		for (Entry<CVRPColumn, Double> entry : parent.getSolution().getColumnMap().entrySet())
		{
			if (!(entry.getKey() instanceof RouteColumn))
			{
				continue;
			}
			RouteColumn routeColumn = (RouteColumn) entry.getKey();
			double value = entry.getValue();
			List<Integer> nodes = routeColumn.getRoute().getNodes();
			for (int i = 0; i < nodes.size() - 1; i++)
			{
				Arc arc = new Arc(nodes.get(i), nodes.get(i + 1));
				if (!arcValueMap.containsKey(arc))
				{
					arcValueMap.put(arc, 0.0);
				}
				arcValueMap.put(arc, arcValueMap.get(arc) + value);
			}
		}

		// Generate branching candidates.
		List<BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem>> candidates = new ArrayList<>();
		double branchingPrecision = Configuration.getConfiguration().getDoubleProperty("BRANCHING_PRECISION");
		for (Arc arc : arcValueMap.keySet())
		{
			double value = arcValueMap.get(arc);
			double fractionalValue = Math.abs(value - Math.rint(value));
			if (fractionalValue > branchingPrecision)
			{
				BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem> candidate = new BranchingCandidate<>(
						fractionalValue);
				candidate.addBranchingDecision(new BranchingDecisionArc(true, arc));
				candidate.addBranchingDecision(new BranchingDecisionArc(false, arc));
				candidates.add(candidate);
			}

		}
		return candidates;
	}
}
