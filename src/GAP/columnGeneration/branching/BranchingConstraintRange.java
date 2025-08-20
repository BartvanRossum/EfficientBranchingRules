package GAP.columnGeneration.branching;

import GAP.GAPInstance;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.OrderColumn;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class BranchingConstraintRange extends AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem>
{
	private final boolean isLowerBound;

	public BranchingConstraintRange(boolean isLowerBound, int bound)
	{
		super(isLowerBound ? ConstraintType.GREATER : ConstraintType.LESSER, bound);

		this.isLowerBound = isLowerBound;
	}

	public boolean isLowerBound()
	{
		return isLowerBound;
	}

	@Override
	public boolean containsColumn(GAPColumn column)
	{
		if (column instanceof OrderColumn)
		{
			OrderColumn orderColumn = (OrderColumn) column;
			if (orderColumn.isMin() && !isLowerBound)
			{
				return true;
			}
			if (orderColumn.isMax() && isLowerBound)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public double getCoefficient(GAPColumn column)
	{
		return 1;
	}
	
	@Override
	public void updateGenericDuals(GAPInstance instance, double dual)
	{
		// Do nothing.
	}

	@Override
	public void updatePricingProblemDuals(GAPPricingProblem pricingProblem, double dual)
	{
		// Do nothing.
	}

	@Override
	public void addSlackVariable(
			AbstractMasterProblem<GAPInstance, GAPColumn, GAPPricingProblem> masterProblem)
			throws IloException
	{
		// Do nothing.
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isLowerBound ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		BranchingConstraintRange other = (BranchingConstraintRange) obj;
		if (isLowerBound != other.isLowerBound) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "BranchingConstraintRange [isLowerBound=" + isLowerBound + ", bound=" + this.bound + "]";
	}
}
