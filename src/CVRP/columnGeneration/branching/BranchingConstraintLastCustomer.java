package CVRP.columnGeneration.branching;

import java.util.List;

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

public class BranchingConstraintLastCustomer extends AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final boolean isAllowed;
	private final int customer;

	public BranchingConstraintLastCustomer(boolean isAllowed, int customer)
	{
		super(AbstractConstraint.ConstraintType.EQUALITY, isAllowed ? 1 : 0);

		this.isAllowed = isAllowed;
		this.customer = customer;
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
		// If customer is not allowed, this is handled in the pricing problem.
		if (bound == 0)
		{
			return;
		}
		if (pricingProblem.getCustomer() != customer)
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
	public void addSlackVariable(
			AbstractMasterProblem<CVRPInstance, CVRPColumn, CVRPPricingProblem> masterProblem)
			throws IloException
	{
		// Very high slack cost.
		masterProblem.addSlackVariable(this, 1, 1000 * 1000, 0, 1);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + customer;
		result = prime * result + (isAllowed ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		BranchingConstraintLastCustomer other = (BranchingConstraintLastCustomer) obj;
		if (customer != other.customer) return false;
		if (isAllowed != other.isAllowed) return false;
		return true;
	}
}
