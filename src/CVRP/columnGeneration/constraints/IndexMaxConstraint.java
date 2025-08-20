package CVRP.columnGeneration.constraints;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.OrderColumn;
import CVRP.columnGeneration.RouteColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPConstants;
import CVRP.instance.CVRPInstance;
import CVRP.instance.CustomerNode;
import graph.structures.digraph.DirectedGraph;
import graph.structures.digraph.DirectedGraphArc;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class IndexMaxConstraint
		extends AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final int vehicleIndex;

	public IndexMaxConstraint(int vehicleIndex, int utility)
	{
		super(ConstraintType.LESSER, -utility);

		this.vehicleIndex = vehicleIndex;
	}

	@Override
	public boolean containsColumn(CVRPColumn column)
	{
		if (column instanceof RouteColumn)
		{
			RouteColumn routeColumn = (RouteColumn) column;
			return routeColumn.getRoute().getVehicleIndex() == vehicleIndex;
		}
		if (column instanceof OrderColumn)
		{
			return ((OrderColumn) column).isMax();
		}
		return false;
	}

	@Override
	public double getCoefficient(CVRPColumn column)
	{
		if (column instanceof RouteColumn)
		{
			return ((RouteColumn) column).getPayoff();
		}
		return -1;
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
		if (pricingProblem.getVehicleIndex() != vehicleIndex)
		{
			return;
		}
		DirectedGraph<CustomerNode, Integer> graph = pricingProblem.getGraph();
		for (DirectedGraphArc<CustomerNode, Integer> arc : graph.getArcs())
		{
			if (CVRPConstants.RESOURCE_IS_DEMAND)
			{
				arc.addDual(dual * arc.getTo().getDemand(), 0);
			}
			else
			{
				arc.addDual(dual * arc.getData(), 0);
			}
		}
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
		IndexMaxConstraint other = (IndexMaxConstraint) obj;
		if (vehicleIndex != other.vehicleIndex) return false;
		return true;
	}
}
