package CVRP.columnGeneration.branching;

import java.util.LinkedHashSet;
import java.util.Set;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import optimisation.BAP.AbstractBranchingDecision;
import optimisation.columnGeneration.AbstractConstraint;

public class BranchingDecisionOrder
		extends AbstractBranchingDecision<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final int index;
	private final boolean isLowerBound;
	private final int bound;

	public BranchingDecisionOrder(int index, boolean isLowerBound, int bound)
	{
		this.index = index;
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
		if (isLowerBound && pricingProblem.getVehicleIndex() <= index)
		{
			pricingProblem.setResourceLowerBound(bound);
		}
		if (!isLowerBound && pricingProblem.getVehicleIndex() >= index)
		{
			pricingProblem.setResourceUpperBound(bound);
		}
	}

	@Override
	public Set<AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>> getBranchingConstraints()
	{
		Set<AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>> constraints = new LinkedHashSet<>();
		constraints.add(new BranchingConstraintOrderRoute(isLowerBound, index, bound));
		constraints.add(new BranchingConstraintOrder(isLowerBound, index, bound));
		return constraints;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + bound;
		result = prime * result + index;
		result = prime * result + (isLowerBound ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		BranchingDecisionOrder other = (BranchingDecisionOrder) obj;
		if (bound != other.bound) return false;
		if (index != other.index) return false;
		if (isLowerBound != other.isLowerBound) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "BranchingDecisionOrder [index=" + index + ", isLowerBound=" + isLowerBound + ", bound=" + bound + "]";
	}
}