package CVRP.columnGeneration.constraints;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.OrderColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class OrderConstraint
		extends AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	public OrderConstraint()
	{
		super(ConstraintType.LESSER, 0);
	}

	@Override
	public boolean containsColumn(CVRPColumn column)
	{
		if (column instanceof OrderColumn)
		{
			return true;
		}
		return false;
	}

	@Override
	public double getCoefficient(CVRPColumn column)
	{
		OrderColumn orderColumn = (OrderColumn) column;
		return orderColumn.isMax() ? -1 : 1;
	}
	
	@Override
	public void addSlackVariable(AbstractMasterProblem<CVRPInstance, CVRPColumn, CVRPPricingProblem> masterProblem)
			throws IloException
	{
		// Do nothing.
	}
	
	@Override
	public void updateGenericDuals(CVRPInstance instance, double dual)
	{
		// Do nothing.
	}

	@Override
	public void updatePricingProblemDuals(CVRPPricingProblem pricingProblem, double dual)
	{
		// Do nothing.
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		return true;
	}
}
