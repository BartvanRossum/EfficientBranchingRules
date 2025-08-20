package GAP.columnGeneration.constraints;

import GAP.GAPInstance;
import GAP.columnGeneration.AssignmentColumn;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class MinimumProfitConstraint extends AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem>
{
	// Note: Since we are in minimisation form, we want to profit to be below a
	// certain negative value.
	public MinimumProfitConstraint(double profit)
	{
		super(ConstraintType.LESSER, profit);

	}

	@Override
	public boolean containsColumn(GAPColumn column)
	{
		return column instanceof AssignmentColumn;
	}

	@Override
	public double getCoefficient(GAPColumn column)
	{
		AssignmentColumn assignmentColumn = (AssignmentColumn) column;
		return assignmentColumn.getProfit();
	}

	@Override
	public void updateGenericDuals(GAPInstance instance, double dual)
	{
		// Do nothing.
	}

	@Override
	public void updatePricingProblemDuals(GAPPricingProblem pricingProblem, double dual)
	{
		pricingProblem.setProfitDual(dual);
	}

	@Override
	public void addSlackVariable(
			AbstractMasterProblem<GAPInstance, GAPColumn, GAPPricingProblem> masterProblem)
			throws IloException
	{
		double slackCost = 1000;
		masterProblem.addSlackVariable(this, -1, slackCost, 0, Double.MAX_VALUE);
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