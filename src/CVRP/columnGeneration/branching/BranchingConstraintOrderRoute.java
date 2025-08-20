package CVRP.columnGeneration.branching;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.RouteColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPConstants;
import CVRP.instance.CVRPInstance;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class BranchingConstraintOrderRoute extends AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final boolean isLowerBound;
	private final int index;
	private final int resourceBound;

	public BranchingConstraintOrderRoute(boolean isLowerBound, int index, int resourceBound)
	{
		super(ConstraintType.LESSER, 0);

		this.isLowerBound = isLowerBound;
		this.index = index;
		this.resourceBound = resourceBound;
	}

	public boolean isLowerBound()
	{
		return isLowerBound;
	}

	@Override
	public boolean containsColumn(CVRPColumn column)
	{
		if (column instanceof RouteColumn)
		{
			RouteColumn routeColumn = (RouteColumn) column;
			int resource = CVRPConstants.RESOURCE_IS_DEMAND ? routeColumn.getRoute().getDemand()
					: routeColumn.getRoute().getDistance();
			int routeIndex = routeColumn.getRoute().getVehicleIndex();
			if (isLowerBound)
			{
				return resource < resourceBound && routeIndex <= index;
			}
			else
			{
				return resource > resourceBound && routeIndex >= index;
			}
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
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + index;
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
		BranchingConstraintOrderRoute other = (BranchingConstraintOrderRoute) obj;
		if (index != other.index) return false;
		if (isLowerBound != other.isLowerBound) return false;
		if (resourceBound != other.resourceBound) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "" + index + " " + isLowerBound + " " + resourceBound;
	}

	@Override
	public void addSlackVariable(AbstractMasterProblem<CVRPInstance, CVRPColumn, CVRPPricingProblem> masterProblem)
			throws IloException
	{
		// Do nothing.
	}
}
