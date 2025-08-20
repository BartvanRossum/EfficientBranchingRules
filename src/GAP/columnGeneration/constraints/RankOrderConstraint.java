package GAP.columnGeneration.constraints;

import GAP.GAPInstance;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.OrderColumn;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class RankOrderConstraint
		extends AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem>
{
	private final int agent;

	public RankOrderConstraint(int agent)
	{
		super(ConstraintType.GREATER, 0);

		this.agent = agent;
	}

	@Override
	public boolean containsColumn(GAPColumn column)
	{
		if (column instanceof OrderColumn)
		{
			int index = ((OrderColumn) column).getIndex();
			return (index == agent || index == agent + 1);
		}
		return false;
	}

	@Override
	public double getCoefficient(GAPColumn column)
	{
		int index = ((OrderColumn) column).getIndex();
		if (index == agent)
		{
			return 1;
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
		// Do nothing.
	}

	@Override
	public void addSlackVariable(AbstractMasterProblem<GAPInstance, GAPColumn, GAPPricingProblem> masterProblem)
			throws IloException
	{
		// Do nothing.
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
		RankOrderConstraint other = (RankOrderConstraint) obj;
		if (agent != other.agent) return false;
		return true;
	}
}
