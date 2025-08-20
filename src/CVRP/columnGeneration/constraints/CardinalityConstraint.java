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

public class CardinalityConstraint extends AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	public CardinalityConstraint(int K)
	{
		super(ConstraintType.EQUALITY, K);
	}

	@Override
	public boolean containsColumn(CVRPColumn column)
	{
		return column instanceof RouteColumn;
	}

	@Override
	public double getCoefficient(CVRPColumn column)
	{
		return 1;
	}

	@Override
	public void addSlackVariable(
			AbstractMasterProblem<CVRPInstance, CVRPColumn, CVRPPricingProblem> masterProblem)
			throws IloException
	{
		// High slack cost.
		masterProblem.addSlackVariable(this, 1, 1000 * 1000, 0, bound);
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
		for (DirectedGraphArc<CustomerNode, Integer> arc : graph.getOutArcs(graph.getNodes().get(0)))
		{
			arc.addDual(dual, 0);
		}
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
