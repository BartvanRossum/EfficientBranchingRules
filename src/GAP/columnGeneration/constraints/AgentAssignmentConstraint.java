package GAP.columnGeneration.constraints;

import GAP.GAPInstance;
import GAP.columnGeneration.AssignmentColumn;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class AgentAssignmentConstraint extends AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem>
{
	private final int agent;

	public AgentAssignmentConstraint(int agent)
	{
		super(ConstraintType.LESSER, 1);

		this.agent = agent;
	}

	public int getAgent()
	{
		return agent;
	}

	@Override
	public boolean containsColumn(GAPColumn column)
	{
		if (column instanceof AssignmentColumn)
		{
			AssignmentColumn assignmentColumn = (AssignmentColumn) column;
			return assignmentColumn.getAgent() == agent;
		}
		return false;
	}

	@Override
	public double getCoefficient(GAPColumn column)
	{
		return 1;
	}
	
	@Override
	public void addSlackVariable(
			AbstractMasterProblem<GAPInstance, GAPColumn, GAPPricingProblem> masterProblem)
			throws IloException
	{
		// Do nothing.
	}

	@Override
	public void updateGenericDuals(GAPInstance instance, double dual)
	{
		// Do nothing.
	}

	@Override
	public void updatePricingProblemDuals(GAPPricingProblem pricingProblem, double dual)
	{
		if (pricingProblem.getAgent() == agent)
		{
			pricingProblem.setAgentDual(dual);
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + agent;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		AgentAssignmentConstraint other = (AgentAssignmentConstraint) obj;
		if (agent != other.agent) return false;
		return true;
	}
}