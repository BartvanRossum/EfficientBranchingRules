package CVRP.columnGeneration.branching;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.OrderColumn;
import CVRP.columnGeneration.RouteColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import optimisation.BAP.AbstractBranchingDecision;
import optimisation.BAP.AbstractBranchingRule;
import optimisation.BAP.BAPNode;
import optimisation.BAP.BranchingCandidate;
import optimisation.columnGeneration.AbstractSolution;

public class BranchingRuleRange extends AbstractBranchingRule<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final double cutOff;

	public BranchingRuleRange(double priority, double cutOff)
	{
		super(priority);

		this.cutOff = cutOff;
	}

	public AbstractBranchingDecision<CVRPInstance, CVRPColumn, CVRPPricingProblem> createBranchingDecision(
			boolean isMinimum, boolean allowed, double value)
	{
		double bound = isMinimum ? value * cutOff : value * (2.0 - cutOff);
		int integerBound;
		if (AbstractSolution.isInteger(bound))
		{
			integerBound = (int) Math.rint(bound);
			if (isMinimum)
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
		return new BranchingDecisionRange(isMinimum, allowed, integerBound);
	}

	@Override
	public List<BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem>> getBranchingCandidates(
			BAPNode<CVRPInstance, CVRPColumn, CVRPPricingProblem> parent)
	{
		// Retrieve value of minimum and maximum.
		double minimum = 0;
		double maximum = 0;
		for (Entry<CVRPColumn, Double> entry : parent.getSolution().getColumnMap().entrySet())
		{
			if (entry.getKey() instanceof RouteColumn)
			{
				continue;
			}
			OrderColumn orderColumn = (OrderColumn) entry.getKey();
			if (orderColumn.isMax())
			{
				maximum = entry.getValue();
			}
			if (orderColumn.isMin())
			{
				minimum = entry.getValue();
			}
		}

		// Check if any routes are used that are below percentage of the minimum.
		double fractionalValueMinimum = 0;
		double fractionalValueMaximum = 0;
		for (Entry<CVRPColumn, Double> entry : parent.getSolution().getColumnMap().entrySet())
		{
			if (!(entry.getKey() instanceof RouteColumn))
			{
				continue;
			}
			RouteColumn routeColumn = (RouteColumn) entry.getKey();
			int resource = routeColumn.getPayoff();
			if (resource < minimum * cutOff)
			{
				fractionalValueMinimum += entry.getValue();
			}
			if (resource > maximum * (2.0 - cutOff))
			{
				fractionalValueMaximum += entry.getValue();
			}
		}

		// Generate branching candidates.
		List<BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem>> candidates = new ArrayList<>();
		if (fractionalValueMinimum > 0.05)
		{
			BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem> candidate = new BranchingCandidate<>(
					fractionalValueMinimum);
			candidate.addBranchingDecision(createBranchingDecision(true, true, minimum));
			candidate.addBranchingDecision(createBranchingDecision(true, false, minimum));
			candidates.add(candidate);
		}
		if (fractionalValueMaximum > 0.05)
		{
			BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem> candidate = new BranchingCandidate<>(
					fractionalValueMaximum);
			candidate.addBranchingDecision(createBranchingDecision(false, true, maximum));
			candidate.addBranchingDecision(createBranchingDecision(false, false, maximum));
			candidates.add(candidate);
		}
		return candidates;

	}
}
