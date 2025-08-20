package CVRP.tsp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.RouteColumn;
import CVRP.columnGeneration.labelling.CVRPLabel;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPConstants;
import CVRP.instance.CVRPInstance;
import CVRP.instance.CustomerNode;
import CVRP.instance.Route;
import graph.structures.digraph.DirectedGraph;
import graph.structures.digraph.DirectedGraphArc;
import optimisation.columnGeneration.AbstractMasterProblem;
import optimisation.columnGeneration.pricing.AbstractPricingProblemSolver;
import util.Pair;

public class TSPBucketSolver extends AbstractPricingProblemSolver<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	public TSPBucketSolver()
	{
		super("labelling");
	}

	@Override
	public List<Pair<CVRPColumn, Double>> generateColumns(
			AbstractMasterProblem<CVRPInstance, CVRPColumn, CVRPPricingProblem> masterProblem,
			CVRPPricingProblem pricingProblem, double reducedCostThreshold, boolean enumerateColumns)
	{
		// Reset label indices.
		int index = 0;

		// Initialise bucket map, mapping (last node, capacity) to a bucket.
		Map<Integer, Map<Integer, List<CVRPLabel>>> forwardBucketMap = new LinkedHashMap<>();
		Map<Integer, Map<Integer, List<CVRPLabel>>> backwardBucketMap = new LinkedHashMap<>();

		// Keep track of the current forward and backward q and number of labels.
		int forwardQ = -1;
		int backwardQ = CVRPConstants.Q + 1;
		if (CVRPConstants.RESOURCE_IS_DEMAND)
		{
			backwardQ = Math.min(pricingProblem.getResourceUpperBound(), CVRPConstants.Q) + 1;
		}
		int forwardLabels = 0;
		int backwardLabels = 0;

		// Retrieve data.
		DirectedGraph<CustomerNode, Integer> graph = pricingProblem.getGraph();

		// Store a list of non-dominated labels and columns.
		Map<Integer, Integer> distanceMap = new LinkedHashMap<>();
		Map<Integer, Set<Pair<CVRPColumn, Double>>> columnMap = new LinkedHashMap<>();

		// Perform labelling.
		while (backwardQ - forwardQ > 1)
		{
			boolean forward = (forwardLabels < backwardLabels) ? true : false;
			if (forward)
			{
				forwardQ++;
			}
			else
			{
				backwardQ--;
			}
			int q = forward ? forwardQ : backwardQ;

			// Retrieve correct bucket map.
			Map<Integer, Map<Integer, List<CVRPLabel>>> bucketMap = forward ? forwardBucketMap : backwardBucketMap;

			// Make initial label with fixed reduced cost.
			if (forward && q == 0)
			{
				forwardLabels += addLabel(new CVRPLabel(index++, null, null, 0, 0, 0), 0, q, bucketMap, pricingProblem);
			}
			else
				if (!forward)
				{
					backwardLabels += addLabel(new CVRPLabel(index++, null, null, 0, 0, 0), 0, q, bucketMap,
							pricingProblem);
				}

			for (int i = 0; i <= CVRPConstants.N; i++)
			{
				// Skip unnecessary iterations.
				if (!bucketMap.containsKey(i) || !bucketMap.get(i).containsKey(q))
				{
					continue;
				}

				for (DirectedGraphArc<CustomerNode, Integer> arc : forward ? graph.getOutArcs(graph.getNodes().get(i))
						: graph.getInArcs(graph.getNodes().get(i)))
				{
					// Skip forbidden arcs.
					if (!pricingProblem.isAllowed(arc))
					{
						continue;
					}

					// Retrieve next customer.
					CustomerNode nextCustomer = forward ? arc.getTo() : arc.getFrom();

					// Backward labels can never be extended to the depot.
					if (!forward && nextCustomer.getCustomer() == 0)
					{
						continue;
					}

					// Capacity check.
					int newQ = forward ? q + arc.getTo().getDemand() : q - arc.getTo().getDemand();
					if (newQ < 0 || newQ > CVRPConstants.Q)
					{
						continue;
					}

					// Skip extensions to labels that will never lead to complete routes.
					if (!forward && newQ < forwardQ)
					{
						continue;
					}
					if (forward && newQ > backwardQ && nextCustomer.getCustomer() != 0)
					{
						continue;
					}

					// Iterate over all labels.
					for (CVRPLabel label : bucketMap.get(i).get(q))
					{
						// We cannot expand to nodes in the memory.
						if ((label.getMemory() & (1 << nextCustomer.getCustomer())) > 0)
						{
							continue;
						}

						// Expand label. Retain all nodes in ng-set of next node, and add next customer.
						int distance = label.getDistance() + arc.getData();
						int memory = label.getMemory();
						memory += 1 << nextCustomer.getCustomer();
						double cost = label.getCost() + arc.getWeight() - arc.getData();
						CVRPLabel expandedLabel = new CVRPLabel(index++, label, arc, cost, memory, distance);

						// Add label to bucket.
						if (forward)
						{
							forwardLabels += addLabel(expandedLabel, nextCustomer.getCustomer(), newQ, bucketMap,
									pricingProblem);
						}
						else
						{
							backwardLabels += addLabel(expandedLabel, nextCustomer.getCustomer(), newQ, bucketMap,
									pricingProblem);
						}

						// Moving back to depot.
						if (nextCustomer.getCustomer() == 0 && forward)
						{
							Pair<CVRPColumn, Double> pair = backtrackPath(expandedLabel,
									pricingProblem.getVehicleIndex());

							// Add the column if it is not dominated.
							if (distanceMap.containsKey(memory))
							{
								if (distanceMap.get(memory) < distance)
								{
									continue;
								}
								if (distanceMap.get(memory) > distance)
								{
									columnMap.put(memory, new LinkedHashSet<>());
								}
							}
							distanceMap.put(memory, distance);
							if (!columnMap.containsKey(memory))
							{
								columnMap.put(memory, new LinkedHashSet<>());
							}
							columnMap.get(memory).add(pair);
						}
					}
				}
			}
		}

		// Concatenate forward and backward labels.
		for (DirectedGraphArc<CustomerNode, Integer> arc : graph.getArcs())
		{
			// Skip forbidden arcs.
			if (!pricingProblem.isAllowed(arc))
			{
				continue;
			}

			// We cannot concatenate at the depot.
			int customerFrom = arc.getFrom().getCustomer();
			int customerTo = arc.getTo().getCustomer();
			if (customerFrom == 0 || customerTo == 0)
			{
				continue;
			}

			for (int q = forwardQ - arc.getTo().getDemand(); q <= forwardQ; q++)
			{
				int nextQ = q + arc.getTo().getDemand();
				if (nextQ < backwardQ)
				{
					continue;
				}

				// Iterate over combinations of forward and backward labels.
				if (!forwardBucketMap.containsKey(customerFrom) || !forwardBucketMap.get(customerFrom).containsKey(q))
				{
					continue;
				}
				if (!backwardBucketMap.containsKey(customerTo) || !backwardBucketMap.get(customerTo).containsKey(nextQ))
				{
					continue;
				}

				for (CVRPLabel forwardLabel : forwardBucketMap.get(customerFrom).get(q))
				{
					backwardLoop:
					for (CVRPLabel backwardLabel : backwardBucketMap.get(customerTo).get(nextQ))
					{
						double reducedCost = forwardLabel.getCost() + backwardLabel.getCost() + arc.getWeight()
								- arc.getData();
						int distance = forwardLabel.getDistance() + backwardLabel.getDistance() + arc.getData();

						// Check memory.
						if ((forwardLabel.getMemory() & backwardLabel.getMemory()) > 0)
						{
							continue backwardLoop;
						}

						// Generate a new column.
						Route route = concatenateLabels(forwardLabel, backwardLabel, arc,
								pricingProblem.getVehicleIndex());
						Pair<CVRPColumn, Double> pair = new Pair<>(new RouteColumn(route), reducedCost);

						// Add the column if it is not dominated.
						int memory = forwardLabel.getMemory() + backwardLabel.getMemory();
						if (distanceMap.containsKey(memory))
						{
							if (distanceMap.get(memory) < distance)
							{
								continue;
							}
							if (distanceMap.get(memory) > distance)
							{
								columnMap.put(memory, new LinkedHashSet<>());
							}
						}
						distanceMap.put(memory, distance);
						if (!columnMap.containsKey(memory))
						{
							columnMap.put(memory, new LinkedHashSet<>());
						}
						columnMap.get(memory).add(pair);
					}
				}
			}
		}

		// Initialise a list of paths.
		ArrayList<Pair<CVRPColumn, Double>> columns = new ArrayList<>();
		for (int memory : columnMap.keySet())
		{
			for (Pair<CVRPColumn, Double> pair : columnMap.get(memory))
			{
				Route route = ((RouteColumn) pair.getKey()).getRoute();
				if (!satisfiesResourceBounds(true, route.getDistance(), route.getDemand(),
						pricingProblem.getResourceLowerBound(), pricingProblem.getResourceUpperBound()))
				{
					continue;
				}
				if (pair.getValue() < reducedCostThreshold)
				{
					columns.add(pair);
				}
			}
		}
		return columns;
	}

	private Pair<CVRPColumn, Double> backtrackPath(CVRPLabel label, int vehicleIndex)
	{
		List<Integer> nodes = new ArrayList<>();
		nodes.add(label.getPreviousArc().getTo().getCustomer());
		int distance = 0;
		int demand = 0;
		double reducedCost = label.getCost();
		CVRPLabel currentLabel = label;

		while (currentLabel.getPreviousLabel() != null)
		{
			nodes.add(0, currentLabel.getPreviousArc().getFrom().getCustomer());
			distance += currentLabel.getPreviousArc().getData();
			demand += currentLabel.getPreviousArc().getFrom().getDemand();
			currentLabel = currentLabel.getPreviousLabel();
		}
		Route route = new Route(vehicleIndex, distance, demand, nodes);
		Pair<CVRPColumn, Double> pair = new Pair<>(new RouteColumn(route), reducedCost);
		return pair;
	}

	private int addLabel(CVRPLabel label, int i, int q, Map<Integer, Map<Integer, List<CVRPLabel>>> bucketMap,
			CVRPPricingProblem pricingProblem)
	{
		// Create new bucket if it does not yet exist.
		if (!bucketMap.containsKey(i))
		{
			bucketMap.put(i, new LinkedHashMap<>());
		}
		if (!bucketMap.get(i).containsKey(q))
		{
			bucketMap.get(i).put(q, new LinkedList<>());
		}
		// Dominance checks.
		int removed = 0;
		ListIterator<CVRPLabel> iterator = bucketMap.get(i).get(q).listIterator();
		while (iterator.hasNext())
		{
			// Retrieve label.
			CVRPLabel otherLabel = iterator.next();

			if (label.getMemory() == otherLabel.getMemory())
			{
				if (label.getDistance() > otherLabel.getDistance())
				{
					return 0;
				}
				if (otherLabel.getDistance() > label.getDistance())
				{
					removed++;
//					iterator.remove();
				}
			}
		}
		bucketMap.get(i).get(q).add(label);
		return 1 - removed;
	}

	private boolean satisfiesResourceBounds(boolean isFinal, int distance, int load, int resourceLowerBound,
			int resourceUpperBound)
	{
		int resource = CVRPConstants.RESOURCE_IS_DEMAND ? load : distance;
		if (isFinal)
		{
			// The resource value will not be modified anymore, and hence it should satisfy
			// both bounds.
			return resource >= resourceLowerBound && resource <= resourceUpperBound;
		}
		else
		{
			// The resources are monotonically increasing, so we need not yet check the
			// lower bound.
			return resource <= resourceUpperBound;
		}
	}

	private Route concatenateLabels(CVRPLabel forwardLabel, CVRPLabel backwardLabel,
			DirectedGraphArc<CustomerNode, Integer> arc, int vehicleIndex)
	{
		List<Integer> nodes = new ArrayList<>();
		int distance = forwardLabel.getDistance() + backwardLabel.getDistance() + arc.getData();
		int demand = 0;

		// Add forward nodes.
		CVRPLabel currentLabel = forwardLabel;
		nodes.add(currentLabel.getPreviousArc().getTo().getCustomer());
		demand += currentLabel.getPreviousArc().getTo().getDemand();
		while (currentLabel.getPreviousLabel() != null)
		{
			nodes.add(0, currentLabel.getPreviousArc().getFrom().getCustomer());
			demand += currentLabel.getPreviousArc().getFrom().getDemand();
			currentLabel = currentLabel.getPreviousLabel();
		}

		// Add backward nodes.
		currentLabel = backwardLabel;
		nodes.add(currentLabel.getPreviousArc().getFrom().getCustomer());
		demand += currentLabel.getPreviousArc().getFrom().getDemand();
		while (currentLabel.getPreviousLabel() != null)
		{
			CustomerNode nextNode = currentLabel.getPreviousArc().getTo();
			nodes.add(nextNode.getCustomer());
			demand += nextNode.getDemand();
			currentLabel = currentLabel.getPreviousLabel();
		}

		// Return route.
		Route route = new Route(vehicleIndex, distance, demand, nodes);
		return route;
	}
}