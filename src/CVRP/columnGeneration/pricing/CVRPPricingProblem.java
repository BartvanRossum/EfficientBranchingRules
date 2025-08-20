package CVRP.columnGeneration.pricing;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import CVRP.columnGeneration.branching.Arc;
import CVRP.instance.CVRPInstance;
import CVRP.instance.CustomerNode;
import graph.structures.digraph.DirectedGraph;
import graph.structures.digraph.DirectedGraphArc;
import optimisation.columnGeneration.pricing.AbstractPricingProblem;

public class CVRPPricingProblem extends AbstractPricingProblem<CVRPInstance>
{
	private final int vehicleIndex;
	private int customer = -1;
	private DirectedGraph<CustomerNode, Integer> graph;
	private final Set<Integer> forbiddenNodes;
	private final Set<Arc> forbiddenArcs;

	private int resourceLowerBound;
	private int resourceUpperBound;

	public CVRPPricingProblem(int vehicleIndex, CVRPInstance instance)
	{
		this.vehicleIndex = vehicleIndex;
		this.graph = instance.getGraph();
		this.forbiddenNodes = new LinkedHashSet<>();
		this.forbiddenArcs = new LinkedHashSet<>();

		this.resourceLowerBound = 0;
		this.resourceUpperBound = Integer.MAX_VALUE;
	}

	public int getResourceLowerBound()
	{
		return resourceLowerBound;
	}

	public void setResourceLowerBound(int resourceLowerBound)
	{
		this.resourceLowerBound = Math.max(this.resourceLowerBound, resourceLowerBound);
	}

	public int getResourceUpperBound()
	{
		return resourceUpperBound;
	}

	public void setResourceUpperBound(int resourceUpperBound)
	{
		this.resourceUpperBound = Math.min(this.resourceUpperBound, resourceUpperBound);
	}

	public boolean isAllowed(int node)
	{
		return !forbiddenNodes.contains(node);
	}

	public boolean isAllowed(DirectedGraphArc<CustomerNode, Integer> arc)
	{
		if (forbiddenNodes.contains(arc.getTo().getCustomer()) || forbiddenNodes.contains(arc.getFrom().getCustomer()))
		{
			return false;
		}
		Arc forbiddenArc = new Arc(arc.getFrom().getCustomer(), arc.getTo().getCustomer());
		if (forbiddenArcs.contains(forbiddenArc))
		{
			return false;
		}
		return true;
	}

	public void setCustomer(int customer)
	{
		this.customer = customer;
	}

	public int getCustomer()
	{
		return customer;
	}

	public void addForbiddenNode(int customer)
	{
		forbiddenNodes.add(customer);
	}

	public void addForbiddenArc(Arc arc)
	{
		forbiddenArcs.add(arc);
	}

	public int getVehicleIndex()
	{
		return vehicleIndex;
	}

	public DirectedGraph<CustomerNode, Integer> getGraph()
	{
		return graph;
	}

	@Override
	public void makeThreadSafe()
	{
		// Dual information is stored in the graph. We need to make a deep copy of this.
		DirectedGraph<CustomerNode, Integer> graphCopy = new DirectedGraph<>();

		// Copy nodes.
		Map<CustomerNode, CustomerNode> nodeCopyMap = new LinkedHashMap<>();
		for (CustomerNode node : graph.getNodes())
		{
			CustomerNode nodeCopy = new CustomerNode(node.getCustomer(), node.getDemand(),
					new LinkedHashSet<>(node.getNeighbours()));
			graphCopy.addNode(nodeCopy);
			nodeCopyMap.put(node, nodeCopy);
		}

		// Copy arcs.
		for (DirectedGraphArc<CustomerNode, Integer> arc : graph.getArcs())
		{
			CustomerNode from = nodeCopyMap.get(arc.getFrom());
			CustomerNode to = nodeCopyMap.get(arc.getTo());

			double[] costs = arc.getCosts().clone();
			double[] duals = arc.getDuals().clone();
			graphCopy.addArc(from, to, arc.getData(), costs, duals);
		}

		// Replace graph by its copy.
		this.graph = graphCopy;
	}
}
