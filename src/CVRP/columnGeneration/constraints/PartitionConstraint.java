package CVRP.columnGeneration.constraints;

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

public class PartitionConstraint extends AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final int customer;

	public PartitionConstraint(int customer)
	{
		super(ConstraintType.EQUALITY, 1);

		this.customer = customer;
	}

	public int getCustomer()
	{
		return customer;
	}

	@Override
	public boolean containsColumn(CVRPColumn column)
	{
		if (column instanceof RouteColumn)
		{
			return ((RouteColumn) column).getRoute().getNodes().contains(customer);
		}
		return false;
	}

	@Override
	public double getCoefficient(CVRPColumn column)
	{
		int coefficient = 0;
		for (int node : ((RouteColumn) column).getRoute().getNodes())
		{
			coefficient += (node == customer) ? 1 : 0;
		}
		return coefficient;
	}
	
	@Override
	public void updateGenericDuals(CVRPInstance instance, double dual)
	{
		// Do nothing.
	}

	@Override
	public void updatePricingProblemDuals(CVRPPricingProblem pricingProblem, double dual)
	{
		DirectedGraph<CustomerNode, Integer> graph = pricingProblem.getGraph();
		for (DirectedGraphArc<CustomerNode, Integer> arc : graph.getInArcs(graph.getNodes().get(customer)))
		{
			arc.addDual(dual, 0);
		}
	}
	
	@Override
	public void addSlackVariable(
			AbstractMasterProblem<CVRPInstance, CVRPColumn, CVRPPricingProblem> masterProblem)
			throws IloException
	{
		// High slack cost.
		masterProblem.addSlackVariable(this, 1, 1000 * 1000, 0, 1);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + customer;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		PartitionConstraint other = (PartitionConstraint) obj;
		if (customer != other.customer) return false;
		return true;
	}
}
