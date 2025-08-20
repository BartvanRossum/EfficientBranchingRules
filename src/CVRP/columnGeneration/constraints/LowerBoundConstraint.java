package CVRP.columnGeneration.constraints;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.OrderColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class LowerBoundConstraint
		extends AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	public LowerBoundConstraint(int bound)
	{
		super(ConstraintType.GREATER, bound);
	}

	@Override
	public boolean containsColumn(CVRPColumn column)
	{
		if (column instanceof OrderColumn)
		{
			return ((OrderColumn) column).isMax();
		}
		return false;
	}

	@Override
	public double getCoefficient(CVRPColumn column)
	{
		return 1;
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
	public void addSlackVariable(AbstractMasterProblem<CVRPInstance, CVRPColumn, CVRPPricingProblem> masterProblem)
			throws IloException
	{
		// Do nothing.
		masterProblem.addSlackVariable(this, 1, 1000 * 1000, 0, bound);
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
