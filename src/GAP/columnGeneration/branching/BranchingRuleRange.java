package GAP.columnGeneration.branching;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import GAP.GAPInstance;
import GAP.columnGeneration.AssignmentColumn;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.OrderColumn;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import optimisation.BAP.AbstractBranchingDecision;
import optimisation.BAP.AbstractBranchingRule;
import optimisation.BAP.BAPNode;
import optimisation.BAP.BranchingCandidate;
import optimisation.columnGeneration.AbstractSolution;
import util.Configuration;

public class BranchingRuleRange extends AbstractBranchingRule<GAPInstance, GAPColumn, GAPPricingProblem>
{
	private final double percentageThreshold = 1;

	public BranchingRuleRange(double priority)
	{
		super(priority);
	}

	private AbstractBranchingDecision<GAPInstance, GAPColumn, GAPPricingProblem> createBranchingDecision(
			boolean isMinimum, boolean allowed, double value)
	{
		double bound = isMinimum ? value * percentageThreshold : value * (2.0 - percentageThreshold);
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
	public List<BranchingCandidate<GAPInstance, GAPColumn, GAPPricingProblem>> getBranchingCandidates(
			BAPNode<GAPInstance, GAPColumn, GAPPricingProblem> parent)
	{
		// Retrieve value of minimum and maximum.
		double minimum = 0;
		double maximum = 0;
		double PRECISION = Configuration.getConfiguration().getDoubleProperty("PRECISION");
		for (Entry<GAPColumn, Double> entry : parent.getSolution().getColumnMap().entrySet())
		{
			if (entry.getKey() instanceof AssignmentColumn)
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
		for (Entry<GAPColumn, Double> entry : parent.getSolution().getColumnMap().entrySet())
		{
			if (!(entry.getKey() instanceof AssignmentColumn))
			{
				continue;
			}
			AssignmentColumn assignmentColumn = (AssignmentColumn) entry.getKey();
			int cost = assignmentColumn.getCost();
			if (cost < minimum * percentageThreshold - PRECISION)
			{
				fractionalValueMinimum += entry.getValue();
			}
			if (cost > maximum * (2.0 - percentageThreshold) + PRECISION)
			{
				fractionalValueMaximum += entry.getValue();
			}
		}

		// Generate branching candidates.
		List<BranchingCandidate<GAPInstance, GAPColumn, GAPPricingProblem>> candidates = new ArrayList<>();
		if (fractionalValueMinimum > PRECISION)
		{
			BranchingCandidate<GAPInstance, GAPColumn, GAPPricingProblem> candidate = new BranchingCandidate<>(
					fractionalValueMinimum);
			candidate.addBranchingDecision(createBranchingDecision(true, true, minimum));
			candidate.addBranchingDecision(createBranchingDecision(true, false, minimum));
			candidates.add(candidate);
		}
		if (fractionalValueMaximum > PRECISION)
		{
			BranchingCandidate<GAPInstance, GAPColumn, GAPPricingProblem> candidate = new BranchingCandidate<>(
					fractionalValueMinimum);
			candidate.addBranchingDecision(createBranchingDecision(false, true, maximum));
			candidate.addBranchingDecision(createBranchingDecision(false, false, maximum));
			candidates.add(candidate);
		}
		return candidates;
	}
}
