package CVRP.columnGeneration.constraints;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.OrderColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class RankOrderConstraint
		extends AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final int vehicleIndex;

	public RankOrderConstraint(int vehicleIndex)
	{
		super(ConstraintType.GREATER, 0);

		this.vehicleIndex = vehicleIndex;
	}

	@Override
	public boolean containsColumn(CVRPColumn column)
	{
		if (column instanceof OrderColumn)
		{
			int index = ((OrderColumn) column).getIndex();
			return (index == vehicleIndex || index == vehicleIndex + 1);
		}
		return false;
	}

	@Override
	public double getCoefficient(CVRPColumn column)
	{
		int index = ((OrderColumn) column).getIndex();
		if (index == vehicleIndex)
		{
			return 1;
		}
		return -1;
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
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + vehicleIndex;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		RankOrderConstraint other = (RankOrderConstraint) obj;
		if (vehicleIndex != other.vehicleIndex) return false;
		return true;
	}
}
