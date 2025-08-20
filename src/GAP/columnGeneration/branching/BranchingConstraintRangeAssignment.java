package GAP.columnGeneration.branching;

import GAP.GAPInstance;
import GAP.columnGeneration.AssignmentColumn;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class BranchingConstraintRangeAssignment extends AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem>
{
	private final boolean isLowerBound;
	private final int resourceBound;

	public BranchingConstraintRangeAssignment(boolean isLowerBound, int resourceBound)
	{
		super(ConstraintType.LESSER, 0);

		this.isLowerBound = isLowerBound;
		this.resourceBound = resourceBound;
	}

	public boolean isLowerBound()
	{
		return isLowerBound;
	}
	
	@Override
	public void addSlackVariable(
			AbstractMasterProblem<GAPInstance, GAPColumn, GAPPricingProblem> masterProblem)
			throws IloException
	{
		// Do nothing.
	}

	@Override
	public boolean containsColumn(GAPColumn column)
	{
		if (column instanceof AssignmentColumn)
		{
			AssignmentColumn assignmentColumn = (AssignmentColumn) column;
			int cost = assignmentColumn.getCost();
			return isLowerBound ? cost < resourceBound : cost > resourceBound;
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
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isLowerBound ? 1231 : 1237);
		result = prime * result + resourceBound;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		BranchingConstraintRangeAssignment other = (BranchingConstraintRangeAssignment) obj;
		if (isLowerBound != other.isLowerBound) return false;
		if (resourceBound != other.resourceBound) return false;
		return true;
	}
}
