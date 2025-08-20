package CVRP.columnGeneration.branching;

import java.util.LinkedHashSet;
import java.util.Set;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPConstants;
import CVRP.instance.CVRPInstance;
import optimisation.BAP.AbstractBranchingDecision;
import optimisation.columnGeneration.AbstractConstraint;

public class BranchingDecisionArc
		extends AbstractBranchingDecision<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final boolean isAllowed;
	private final Arc arc;

	public BranchingDecisionArc(boolean isAllowed, Arc arc)
	{
		this.isAllowed = isAllowed;
		this.arc = arc;
	}

	public Arc getArc()
	{
		return arc;
	}

	@Override
	public boolean isCompatible(CVRPPricingProblem pricingProblem)
	{
		return true;
	}

	@Override
	public void modifyPricingProblem(CVRPPricingProblem pricingProblem)
	{
		if (!isAllowed)
		{
			// Deactivate this particular arc.
			pricingProblem.addForbiddenArc(arc);
		}
		else
		{
			// Suppose that arc (i, j) is selected. We remove all (i, k) and all (k, j).
			// Note, we can not dispose of arcs entering or leaving the depot.
			for (int i = 0; i <= CVRPConstants.N; i++)
			{
				if (i != arc.getFrom() && arc.getTo() != 0)
				{
					pricingProblem.addForbiddenArc(new Arc(i, arc.getTo()));
				}
				if (i != arc.getTo() && arc.getFrom() != 0)
				{
					pricingProblem.addForbiddenArc(new Arc(arc.getFrom(), i));
				}
			}
		}
	}

	@Override
	public Set<AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>> getBranchingConstraints()
	{
		Set<AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>> constraints = new LinkedHashSet<>();
		int bound = isAllowed ? 1 : 0;
		constraints.add(new BranchingConstraintArc(bound, arc));
		return constraints;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arc == null) ? 0 : arc.hashCode());
		result = prime * result + (isAllowed ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		BranchingDecisionArc other = (BranchingDecisionArc) obj;
		if (arc == null)
		{
			if (other.arc != null) return false;
		}
		else if (!arc.equals(other.arc)) return false;
		if (isAllowed != other.isAllowed) return false;
		return true;
	}
}