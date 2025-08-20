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

public class BranchingConstraintArc extends AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final Arc arc;

	public BranchingConstraintArc(int bound, Arc arc)
	{
		super(AbstractConstraint.ConstraintType.EQUALITY, bound);

		this.arc = arc;
	}

	@Override
	public boolean containsColumn(CVRPColumn column)
	{
		if (column instanceof RouteColumn)
		{
			RouteColumn routeColumn = (RouteColumn) column;
			List<Integer> nodes = routeColumn.getRoute().getNodes();
			for (int i = 0; i < nodes.size() - 1; i++)
			{
				if (nodes.get(i) == arc.getFrom() && nodes.get(i + 1) == arc.getTo())
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public double getCoefficient(CVRPColumn column)
	{
		int coefficient = 0;
		List<Integer> nodes = ((RouteColumn) column).getRoute().getNodes();
		for (int i = 0; i < nodes.size() - 1; i++)
		{
			if (nodes.get(i) == arc.getFrom() && nodes.get(i + 1) == arc.getTo())
			{
				coefficient++;
			}
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
		// If edge is not allowed, this is handled in the pricing problem.
		if (bound == 0)
		{
			return;
		}

		DirectedGraph<CustomerNode, Integer> graph = pricingProblem.getGraph();
		for (DirectedGraphArc<CustomerNode, Integer> outArc : graph.getOutArcs(graph.getNodes().get(arc.getFrom())))
		{
			if (outArc.getTo().getCustomer() == arc.getTo())
			{
				outArc.addDual(dual, 0);
				break;
			}
		}
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
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((arc == null) ? 0 : arc.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		BranchingConstraintArc other = (BranchingConstraintArc) obj;
		if (arc == null)
		{
			if (other.arc != null) return false;
		}
		else if (!arc.equals(other.arc)) return false;
		return true;
	}
}
