package CVRP.columnGeneration.branching;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.OrderColumn;
import CVRP.columnGeneration.RouteColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPConstants;
import CVRP.instance.CVRPInstance;
import optimisation.BAP.AbstractBranchingDecision;
import optimisation.BAP.AbstractBranchingRule;
import optimisation.BAP.BAPNode;
import optimisation.BAP.BranchingCandidate;
import optimisation.columnGeneration.AbstractSolution;

public class BranchingRuleOrder extends AbstractBranchingRule<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final double cutOff;

	public BranchingRuleOrder(double priority, double cutOff)
	{
		super(priority);

		this.cutOff = cutOff;
	}

	public AbstractBranchingDecision<CVRPInstance, CVRPColumn, CVRPPricingProblem> createBranchingDecision(
			boolean isLower, boolean allowed, int index, double value)
	{
		double bound = isLower ? value * cutOff : value * (2.0 - cutOff);
		int integerBound;
		if (AbstractSolution.isInteger(bound))
		{
			integerBound = (int) Math.rint(bound);
			if (isLower)
			{
				integerBound = allowed ? integerBound : integerBound - 1;
			}
			else
			{
				integerBound = allowed ? integerBound + 1 : integerBound;
			}
		}
		else
		{
			integerBound = allowed ? (int) Math.ceil(bound) : (int) Math.floor(bound);
		}
		return new BranchingDecisionOrder(index, allowed, integerBound);
	}

	@Override
	public List<BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem>> getBranchingCandidates(
			BAPNode<CVRPInstance, CVRPColumn, CVRPPricingProblem> parent)
	{
		// Reset values.
		Map<Integer, Double> orderValues = new LinkedHashMap<>();
		Map<Integer, Double> orderLowerViolation = new LinkedHashMap<>();
		Map<Integer, Double> orderUpperViolation = new LinkedHashMap<>();
		for (int k = 0; k < CVRPConstants.K; k++)
		{
			orderLowerViolation.put(k, 0.0);
			orderUpperViolation.put(k, 0.0);
		}

		// Retrieve value of minimum.
		for (Entry<CVRPColumn, Double> entry : parent.getSolution().getColumnMap().entrySet())
		{
			if (entry.getKey() instanceof RouteColumn)
			{
				continue;
			}
			OrderColumn orderColumn = (OrderColumn) entry.getKey();
			orderValues.put(orderColumn.getIndex(), entry.getValue());
		}

		// Check if any routes are used that are below percentage of the minimum.
		for (Entry<CVRPColumn, Double> entry : parent.getSolution().getColumnMap().entrySet())
		{
			if (!(entry.getKey() instanceof RouteColumn))
			{
				continue;
			}
			RouteColumn routeColumn = (RouteColumn) entry.getKey();
			int resource = routeColumn.getPayoff();
			int index = routeColumn.getRoute().getVehicleIndex();
			for (int k = 0; k < CVRPConstants.K; k++)
			{
				double lowerCutOff = cutOff * orderValues.get(k);
				double upperCutOff = (2.0 - cutOff) * orderValues.get(k);
				if (index <= k && resource < lowerCutOff)
				{
					orderLowerViolation.put(k, orderLowerViolation.get(k) + entry.getValue());
				}
				if (index >= k && resource > upperCutOff)
				{
					orderUpperViolation.put(k, orderUpperViolation.get(k) + entry.getValue());
				}
			}
		}

		// Generate branching candidates.
		List<BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem>> candidates = new ArrayList<>();
		for (int k = 0; k < CVRPConstants.K; k++)
		{
			if (orderLowerViolation.get(k) > 0.05)
			{
				double value = orderValues.get(k);
				BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem> candidate = new BranchingCandidate<>(
						orderLowerViolation.get(k));
				candidate.addBranchingDecision(createBranchingDecision(true, true, k, value));
				candidate.addBranchingDecision(createBranchingDecision(true, false, k, value));
				candidates.add(candidate);
			}
			if (orderUpperViolation.get(k) > 0.05)
			{
				double value = orderValues.get(k);
				BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem> candidate = new BranchingCandidate<>(
						orderUpperViolation.get(k));
				candidate.addBranchingDecision(createBranchingDecision(false, true, k, value));
				candidate.addBranchingDecision(createBranchingDecision(false, false, k, value));
				candidates.add(candidate);
			}
		}
		return candidates;
	}
}
