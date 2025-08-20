package CVRP.columnGeneration.branching;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.OrderColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class BranchingConstraintOrder extends AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final int index;

	public BranchingConstraintOrder(boolean isLowerBound, int index, int bound)
	{
		super(isLowerBound ? ConstraintType.GREATER : ConstraintType.LESSER, bound);

		this.index = index;
	}

	public int getIndex()
	{
		return index;
	}

	@Override
	public boolean containsColumn(CVRPColumn column)
	{
		if (column instanceof OrderColumn)
		{
			OrderColumn orderColumn = (OrderColumn) column;
			return orderColumn.getIndex() == index;
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
	public void addSlackVariable(
			AbstractMasterProblem<CVRPInstance, CVRPColumn, CVRPPricingProblem> masterProblem)
			throws IloException
	{
		// High slack cost.
		masterProblem.addSlackVariable(this, 1, 1000 * 1000, 0, bound);
		masterProblem.addSlackVariable(this, -1, 1000 * 1000, 0, bound);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		BranchingConstraintOrder other = (BranchingConstraintOrder) obj;
		if (index != other.index) return false;
		return true;
	}
}
