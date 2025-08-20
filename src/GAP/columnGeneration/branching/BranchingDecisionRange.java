package GAP.columnGeneration.branching;

import java.util.LinkedHashSet;
import java.util.Set;

import GAP.GAPInstance;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import optimisation.BAP.AbstractBranchingDecision;
import optimisation.columnGeneration.AbstractConstraint;

public class BranchingDecisionRange
		extends AbstractBranchingDecision<GAPInstance, GAPColumn, GAPPricingProblem>
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
	public boolean isCompatible(GAPPricingProblem pricingProblem)
	{
		return true;
	}

	@Override
	public void modifyPricingProblem(GAPPricingProblem pricingProblem)
	{
		if (isMinimum && isLowerBound)
		{
			pricingProblem.setCostLowerBound(bound);
		}
		if (!isMinimum && !isLowerBound)
		{
			pricingProblem.setCostUpperBound(bound);
		}
	}

	@Override
	public Set<AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem>> getBranchingConstraints()
	{
		Set<AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem>> constraints = new LinkedHashSet<>();
		if ((isMinimum && isLowerBound) || (!isMinimum && !isLowerBound))
		{
			constraints.add(new BranchingConstraintRangeAssignment(isLowerBound, bound));
		}
		else
		{
			constraints.add(new BranchingConstraintRange(isLowerBound, bound));
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