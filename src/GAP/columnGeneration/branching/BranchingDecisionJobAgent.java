package GAP.columnGeneration.branching;

import java.util.LinkedHashSet;
import java.util.Set;

import GAP.GAPInstance;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import optimisation.BAP.AbstractBranchingDecision;
import optimisation.columnGeneration.AbstractConstraint;

public class BranchingDecisionJobAgent extends AbstractBranchingDecision<GAPInstance, GAPColumn, GAPPricingProblem>
{
	private final int agent;
	private final int job;
	private final boolean allowed;

	public BranchingDecisionJobAgent(int agent, int job, boolean allowed)
	{
		this.agent = agent;
		this.job = job;
		this.allowed = allowed;
	}

	@Override
	public Set<AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem>> getBranchingConstraints()
	{
		Set<AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem>> constraints = new LinkedHashSet<>();
		constraints.add(new BranchingConstraintJobAgent(agent, job, allowed));
		return constraints;
	}

	@Override
	public boolean isCompatible(GAPPricingProblem pricingProblem)
	{
		return true;
	}

	@Override
	public void modifyPricingProblem(GAPPricingProblem pricingProblem)
	{
		if (pricingProblem.getAgent() == agent)
		{
			if (allowed)
			{
				pricingProblem.addForcedJob(job);
			}
			else
			{
				pricingProblem.addForbiddenJob(job);
			}
		}
		else
		{
			if (allowed)
			{
				pricingProblem.addForbiddenJob(job);
			}
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + agent;
		result = prime * result + (allowed ? 1231 : 1237);
		result = prime * result + job;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		BranchingDecisionJobAgent other = (BranchingDecisionJobAgent) obj;
		if (agent != other.agent) return false;
		if (allowed != other.allowed) return false;
		if (job != other.job) return false;
		return true;
	}
}
