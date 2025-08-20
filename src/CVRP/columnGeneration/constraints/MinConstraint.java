package CVRP.columnGeneration.constraints;

import java.util.List;

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

public class MinConstraint
		extends AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final int customer;
	private final int M;

	public MinConstraint(int customer, int M)
	{
		super(ConstraintType.GREATER, -M);

		this.customer = customer;
		this.M = M;
	}

	@Override
	public boolean containsColumn(CVRPColumn column)
	{
		if (column instanceof RouteColumn)
		{
			RouteColumn routeColumn = (RouteColumn) column;
			List<Integer> nodes = routeColumn.getRoute().getNodes();
			return nodes.get(nodes.size() - 2) == customer;
		}
		if (column instanceof OrderColumn)
		{
			return ((OrderColumn) column).isMin();
		}
		return false;
	}

	@Override
	public double getCoefficient(CVRPColumn column)
	{
		if (column instanceof RouteColumn)
		{
			return ((RouteColumn) column).getPayoff() - M;
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
		if (pricingProblem.getCustomer() != customer)
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

			// Add the big-M dual on the final arc to the depot.
			if (arc.getTo().getCustomer() == 0)
			{
				arc.addDual(dual * -M, 0);
			}
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + M;
		result = prime * result + customer;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		MinConstraint other = (MinConstraint) obj;
		if (M != other.M) return false;
		if (customer != other.customer) return false;
		return true;
	}
}
