package GAP.columnGeneration.constraints;

import GAP.GAPInstance;
import GAP.columnGeneration.AssignmentColumn;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.OrderColumn;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class AgentMinConstraint extends AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem>
{
	private final int agent;

	public AgentMinConstraint(int agent)
	{
		super(ConstraintType.GREATER, 0);

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
		else
		{
			OrderColumn orderColumn = (OrderColumn) column;
			return orderColumn.isMin();
		}
	}

	@Override
	public void addSlackVariable(
			AbstractMasterProblem<GAPInstance, GAPColumn, GAPPricingProblem> masterProblem)
			throws IloException
	{
		// Do nothing.
	}

	@Override
	public double getCoefficient(GAPColumn column)
	{
		if (column instanceof AssignmentColumn)
		{
			AssignmentColumn assignmentColumn = (AssignmentColumn) column;
			return assignmentColumn.getCost();
		}
		return -1;
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
			pricingProblem.addCostDual(dual);
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
		AgentMinConstraint other = (AgentMinConstraint) obj;
		if (agent != other.agent) return false;
		return true;
	}
}
