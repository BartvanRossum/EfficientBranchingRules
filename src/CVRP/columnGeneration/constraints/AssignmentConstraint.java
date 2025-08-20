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

public class AssignmentConstraint extends AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final int vehicleIndex;

	public AssignmentConstraint(int vehicleIndex)
	{
		super(ConstraintType.EQUALITY, 1);

		this.vehicleIndex = vehicleIndex;
	}

	public int getVehicleIndex()
	{
		return vehicleIndex;
	}

	@Override
	public boolean containsColumn(CVRPColumn column)
	{
		if (column instanceof RouteColumn)
		{
			return ((RouteColumn) column).getRoute().getVehicleIndex() == vehicleIndex;
		}
		return false;
	}

	@Override
	public double getCoefficient(CVRPColumn column)
	{
		return 1;
	}

	@Override
	public void addSlackVariable(AbstractMasterProblem<CVRPInstance, CVRPColumn, CVRPPricingProblem> masterProblem)
			throws IloException
	{
		masterProblem.addSlackVariable(this, 1, 1000 * 1000, 0, 1);
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
		for (DirectedGraphArc<CustomerNode, Integer> arc : graph.getOutArcs(graph.getNodes().get(0)))
		{
			arc.addDual(dual, 0);
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
		AssignmentConstraint other = (AssignmentConstraint) obj;
		if (vehicleIndex != other.vehicleIndex) return false;
		return true;
	}
}
