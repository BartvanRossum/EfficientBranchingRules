package CVRP.columnGeneration.branching;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.RouteColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import CVRP.instance.CustomerNode;
import graph.structures.digraph.DirectedGraph;
import graph.structures.digraph.DirectedGraphArc;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class BranchingConstraintVehicleCustomer extends AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final boolean isAllowed;
	private final int vehicleIndex;
	private final int customer;

	public BranchingConstraintVehicleCustomer(boolean isAllowed, int vehicleIndex, int customer)
	{
		super(AbstractConstraint.ConstraintType.EQUALITY, 0);

		this.isAllowed = isAllowed;
		this.vehicleIndex = vehicleIndex;
		this.customer = customer;
	}

	@Override
	public boolean containsColumn(CVRPColumn column)
	{
		if (column instanceof RouteColumn)
		{
			RouteColumn routeColumn = (RouteColumn) column;
			if (isAllowed)
			{
				return (routeColumn.getRoute().getVehicleIndex() != vehicleIndex
						&& routeColumn.getRoute().getNodes().contains(customer));
			}
			else
			{
				return (routeColumn.getRoute().getVehicleIndex() == vehicleIndex
						&& routeColumn.getRoute().getNodes().contains(customer));
			}
		}
		return false;
	}

	@Override
	public double getCoefficient(CVRPColumn column)
	{
		int coefficient = 0;
		for (int node : ((RouteColumn) column).getRoute().getNodes())
		{
			if (node == customer)
			{
				coefficient++;
			}
		}
		return coefficient;
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
		// If customer is not allowed, this is handled in the pricing problem.
		if (bound == 0)
		{
			return;
		}
		if (pricingProblem.getVehicleIndex() != vehicleIndex)
		{
			return;
		}

		DirectedGraph<CustomerNode, Integer> graph = pricingProblem.getGraph();
		for (DirectedGraphArc<CustomerNode, Integer> outArc : graph.getOutArcs(graph.getNodes().get(customer)))
		{
			outArc.addDual(dual, 0);
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + customer;
		result = prime * result + (isAllowed ? 1231 : 1237);
		result = prime * result + vehicleIndex;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		BranchingConstraintVehicleCustomer other = (BranchingConstraintVehicleCustomer) obj;
		if (customer != other.customer) return false;
		if (isAllowed != other.isAllowed) return false;
		if (vehicleIndex != other.vehicleIndex) return false;
		return true;
	}
}
