package CVRP.columnGeneration.branching;

import java.util.LinkedHashSet;
import java.util.Set;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPConstants;
import CVRP.instance.CVRPInstance;
import optimisation.BAP.AbstractBranchingDecision;
import optimisation.columnGeneration.AbstractConstraint;

public class BranchingDecisionRange
		extends AbstractBranchingDecision<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final boolean isMinimum;
	private final boolean isLowerBound;
	private final int bound;

	public BranchingDecisionRange(boolean isMinimum, boolean isLowerBound, int bound)
	{
		this.isMinimum = isMinimum;
		this.isLowerBound = isLowerBound;
		this.bound = bound;
	}

	@Override
	public boolean isCompatible(CVRPPricingProblem pricingProblem)
	{
		return true;
	}

	@Override
	public void modifyPricingProblem(CVRPPricingProblem pricingProblem)
	{
		if (isMinimum && isLowerBound)
		{
			pricingProblem.setResourceLowerBound(bound);
		}

		if (!isMinimum && !isLowerBound)
		{
			pricingProblem.setResourceUpperBound(bound);
		}
	}

	@Override
	public Set<AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>> getBranchingConstraints()
	{
		Set<AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>> constraints = new LinkedHashSet<>();
		if ((isMinimum && isLowerBound) || (!isMinimum && !isLowerBound))
		{
			constraints.add(new BranchingConstraintRangeRoute(isLowerBound, bound));
		}
		else
		{
			int index = isMinimum ? CVRPConstants.K - 1 : 0;
			constraints.add(new BranchingConstraintOrder(isLowerBound, index, bound));
		}
		return constraints;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + bound;
		result = prime * result + (isLowerBound ? 1231 : 1237);
		result = prime * result + (isMinimum ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		BranchingDecisionRange other = (BranchingDecisionRange) obj;
		if (bound != other.bound) return false;
		if (isLowerBound != other.isLowerBound) return false;
		if (isMinimum != other.isMinimum) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "" + isMinimum + " " + isLowerBound + " " + bound;
	}
}