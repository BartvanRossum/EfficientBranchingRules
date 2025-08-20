package CVRP.columnGeneration.labelling;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.RouteColumn;
import CVRP.columnGeneration.branching.Arc;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPConstants;
import CVRP.instance.CVRPInstance;
import CVRP.instance.CustomerNode;
import CVRP.instance.Route;
import graph.structures.digraph.DirectedGraph;
import graph.structures.digraph.DirectedGraphArc;
import optimisation.columnGeneration.AbstractMasterProblem;
import optimisation.columnGeneration.PricingSettings;
import optimisation.columnGeneration.pricing.AbstractPricingProblemSolver;
import util.Configuration;
import util.Pair;

public class CVRPBucketSolver
		extends AbstractPricingProblemSolver<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	public CVRPBucketSolver()
	{
		super("labelling");
	}

	@Override
	public List<Pair<CVRPColumn, Double>> generateColumns(
			AbstractMasterProblem<CVRPInstance, CVRPColumn, CVRPPricingProblem> masterProblem,
			CVRPPricingProblem pricingProblem, double reducedCostThreshold, boolean enumerateColumns)
	{
		// Retrieve pricing parameters.
		final int K = Configuration.getConfiguration().getIntProperty("K");

		// Preprocess pricing problem.
		preprocessForbiddenArcs(pricingProblem);

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

		// Initialise a list of paths.
		List<Pair<CVRPColumn, Double>> columns = new ArrayList<>();

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

			// Retrieve correct bucketmap.
			Map<Integer, Map<Integer, List<CVRPLabel>>> bucketMap = forward ? forwardBucketMap
					: backwardBucketMap;

			// Make initial label with fixed reduced cost.
			if (forward && q == 0)
			{
				forwardLabels += addLabel(new CVRPLabel(index++, null, null, 0, 0, 0),
						0, q, bucketMap, pricingProblem);
			}
			else if (!forward)
			{
				backwardLabels += addLabel(new CVRPLabel(index++, null, null, 0, 0, 0),
						0, q, bucketMap, pricingProblem);
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

						// Resource bound check.
						int distance = label.getDistance() + arc.getData();
						if (!satisfiesResourceBounds(false, distance, newQ, pricingProblem.getResourceLowerBound(),
								pricingProblem.getResourceUpperBound()))
						{
							continue;
						}

						// Expand label. Retain all nodes in ng-set of next node, and add next customer.
						int memory = label.getMemory() & nextCustomer.getBitwiseNeighbours();
						memory += 1 << nextCustomer.getCustomer();
						double cost = label.getCost() + arc.getWeight() - arc.getData();
						CVRPLabel expandedLabel = new CVRPLabel(index++, label, arc, cost, memory,
								distance);

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

						// Moving back to depot, so we check the reduced cost criterion.
						if (cost < reducedCostThreshold && nextCustomer.getCustomer() == 0 && forward)
						{
							// Resource bound check.
							if (!satisfiesResourceBounds(true, distance, newQ, pricingProblem.getResourceLowerBound(),
									pricingProblem.getResourceUpperBound()))
							{
								continue;
							}
							columns.add(backtrackPath(expandedLabel, pricingProblem.getVehicleIndex()));
							if (columns.size() == K)
							{
								return columns;
							}
						}
					}
				}
			}
		}

		// Concatenate forward and backward labels.
		for (DirectedGraphArc<CustomerNode, Integer> arc : graph.getArcs())
		{
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

				forwardLoop: for (CVRPLabel forwardLabel : forwardBucketMap.get(customerFrom).get(q))
				{
					backwardLoop: for (CVRPLabel backwardLabel : backwardBucketMap.get(customerTo).get(nextQ))
					{
						double reducedCost = forwardLabel.getCost() + backwardLabel.getCost() + arc.getWeight()
								- arc.getData();
						if (reducedCost >= reducedCostThreshold)
						{
							break forwardLoop;
						}

						// Resource bound check.
						int distance = forwardLabel.getDistance() + backwardLabel.getDistance() + arc.getData();
						if (!satisfiesResourceBounds(true, distance, nextQ, pricingProblem.getResourceLowerBound(),
								pricingProblem.getResourceUpperBound()))
						{
							continue backwardLoop;
						}

						// Check memory.
						if ((forwardLabel.getMemory() & backwardLabel.getMemory()) > 0)
						{
							continue backwardLoop;
						}

						// Generate a new column.
						Route route = concatenateLabels(forwardLabel, backwardLabel, arc,
								pricingProblem.getVehicleIndex());
						Pair<CVRPColumn, Double> pair = new Pair<>(new RouteColumn(route), reducedCost);
						columns.add(pair);
						if (columns.size() == K)
						{
							return columns;
						}
					}
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
		boolean inserted = false;
		ListIterator<CVRPLabel> iterator = bucketMap.get(i).get(q).listIterator();
		while (iterator.hasNext())
		{
			// Retrieve label.
			CVRPLabel otherLabel = iterator.next();

			// First check if a label is dominated by another label with lower or equal
			// reduced cost.
			if (label.getCost() >= otherLabel.getCost())
			{
				if (dominates(otherLabel, label, pricingProblem.getResourceLowerBound(),
						pricingProblem.getResourceUpperBound()))
				{
					return 0;
				}
			}

			// The label is not yet inserted and not dominated, so we add it to the bucket.
			if (label.getCost() < otherLabel.getCost() && !inserted)
			{
				inserted = true;
				iterator.previous();
				iterator.add(label);
				otherLabel = iterator.next();
			}

			// We check if the new label dominates any labels in the current bucket.
			if (label.getCost() <= otherLabel.getCost())
			{
				if (dominates(label, otherLabel, pricingProblem.getResourceLowerBound(),
						pricingProblem.getResourceUpperBound()))
				{
					removed++;
					iterator.remove();
				}
			}
		}

		// Add to the bucket if not yet added.
		if (!inserted)
		{
			bucketMap.get(i).get(q).add(label);
		}
		return 1 - removed;
	}

	private boolean dominates(CVRPLabel firstLabel, CVRPLabel secondLabel, int resourceLowerBound,
			int resourceUpperBound)
	{
		// In case of heuristic pricing: the first label, having a lower reduced cost,
		// always dominates the second one.
		if (!PricingSettings.EXACT_PRICING)
		{
			return true;
		}

		// In order to have dominance, the NG-neighbourhood should be fully contained.
		if ((firstLabel.getMemory() & secondLabel.getMemory()) < firstLabel.getMemory())
		{
			return false;
		}

		// We consider the load resource.
		if (CVRPConstants.RESOURCE_IS_DEMAND)
		{
			// The load of two compared labels is always identical.
			return true;
		}

		// We consider the distance resource.
		if (resourceLowerBound == 0)
		{
			if (resourceUpperBound == Integer.MAX_VALUE)
			{
				// Both bounds inactive, so apply regular dominance criteria.
				return true;
			}
			else
			{
				// Only UB is active, so we prefer the label with lowest distance.
				return firstLabel.getDistance() <= secondLabel.getDistance();
			}
		}
		else
		{
			if (resourceUpperBound == Integer.MAX_VALUE)
			{
				// Only LB is active. We prefer the first label if it satisfies the LB already,
				// or if it is closer to satisfying the LB.
				return firstLabel.getDistance() >= resourceLowerBound
						|| firstLabel.getDistance() >= secondLabel.getDistance();
			}
			else
			{
				// Both LB and UB are active. We can only prove dominance if the distances are
				// equal.
				return firstLabel.getDistance() == secondLabel.getDistance();
			}
		}
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

	private void preprocessForbiddenArcs(CVRPPricingProblem pricingProblem)
	{
		// Retrieve graph.
		DirectedGraph<CustomerNode, Integer> graph = pricingProblem.getGraph();
		CustomerNode depot = graph.getNodes().get(0);

		// Remove all customers less than vehicle index.
		if (!CVRPConstants.USE_U)
		{
			for (int i = 1; i <= pricingProblem.getVehicleIndex(); i++)
			{
				pricingProblem.addForbiddenNode(i);
			}
		}

		// Preprocess to remove forbidden arcs.
		int customer = pricingProblem.getCustomer();
		if (customer > 0)
		{
			// We can only reach the depot from this customer.
			for (DirectedGraphArc<CustomerNode, Integer> sinkArc : graph.getInArcs(depot))
			{
				if (sinkArc.getFrom().getCustomer() != customer)
				{
					Arc arc = new Arc(sinkArc.getFrom().getCustomer(), 0);
					pricingProblem.addForbiddenArc(arc);
				}
			}

			// We must reach the depot after this customer.
			for (DirectedGraphArc<CustomerNode, Integer> outArc : graph.getOutArcs(graph.getNodes().get(customer)))
			{
				if (outArc.getTo().getCustomer() != 0)
				{
					Arc arc = new Arc(customer, outArc.getTo().getCustomer());
					pricingProblem.addForbiddenArc(arc);
				}
			}

			// The index of the start customer may not be greater than the last customer.
			for (DirectedGraphArc<CustomerNode, Integer> sourceArc : graph.getOutArcs(depot))
			{
				if (sourceArc.getTo().getCustomer() > customer)
				{
					Arc arc = new Arc(0, sourceArc.getTo().getCustomer());
					pricingProblem.addForbiddenArc(arc);
				}
			}
		}
	}
}