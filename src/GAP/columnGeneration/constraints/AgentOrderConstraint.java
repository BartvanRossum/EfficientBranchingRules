package GAP.columnGeneration.constraints;

import GAP.GAPInstance;
import GAP.columnGeneration.AssignmentColumn;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.OrderColumn;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class AgentOrderConstraint
		extends AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem>
{
	private final int agent;

	public AgentOrderConstraint(int agent)
	{
		super(ConstraintType.EQUALITY, 0);

		this.agent = agent;
	}

	@Override
	public boolean containsColumn(GAPColumn column)
	{
		if (column instanceof AssignmentColumn)
		{
			AssignmentColumn assignmentColumn = (AssignmentColumn) column;
			return assignmentColumn.getAgent() == agent;
		}
		if (column instanceof OrderColumn)
		{
			return ((OrderColumn) column).getIndex() == agent;
		}
		return false;
	}

	@Override
	public double getCoefficient(GAPColumn column)
	{
		if (column instanceof AssignmentColumn)
		{
			return ((AssignmentColumn) column).getCost();
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
		if (pricingProblem.getAgent() != agent)
		{
			return;
		}
		pricingProblem.addCostDual(dual);
	}

	@Override
	public void addSlackVariable(
			AbstractMasterProblem<GAPInstance, GAPColumn, GAPPricingProblem> masterProblem)
			throws IloException
	{
		// High slack cost.
		masterProblem.addSlackVariable(this, 1, 1000 * 1000, 0, 1000 * 1000);
		masterProblem.addSlackVariable(this, 1, -1000 * 1000, -1000 * 1000, 0);
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
		AgentOrderConstraint other = (AgentOrderConstraint) obj;
		if (agent != other.agent) return false;
		return true;
	}
}
