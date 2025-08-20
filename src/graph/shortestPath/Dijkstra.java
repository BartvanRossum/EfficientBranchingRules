package graph.shortestPath;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import graph.structures.digraph.DirectedGraph;
import graph.structures.digraph.DirectedGraphArc;
import graph.structures.digraph.DirectedGraphNodeIndex;

public class Dijkstra<V extends DirectedGraphNodeIndex, A>
{
	private final DirectedGraph<V, A> graph;
	private final V origin;
	private final int numNodes;

	private double[] distance;
	private int[] previous;
	private DirectedGraphArc<V, A>[] previousArc;
	private final Set<V> settled;

	private V destination = null;

	public Dijkstra(DirectedGraph<V, A> graph, V origin)
	{
		this.graph = graph;
		this.origin = origin;

		this.settled = new LinkedHashSet<>();
		graph.setNodeIndices();
		this.numNodes = graph.getNumberOfNodes();
	}

	public void setDestination(V destination)
	{
		this.destination = destination;
	}

	@SuppressWarnings("unchecked")
	public void computeDistances()
	{
		// Initialise distance matrix.
		this.distance = new double[numNodes];

		// Initialise successor arrays;
		this.previous = new int[numNodes];
		this.previousArc = (DirectedGraphArc<V, A>[]) Array.newInstance(DirectedGraphArc.class, numNodes);

		// Initialise queue.
		PriorityQueue<Label> queue = new PriorityQueue<>(new LabelDistanceComparator());

		// Initiliase arrays.
		for (V node : graph.getNodes())
		{
			distance[node.getNodeIndex()] = Double.MAX_VALUE;
		}
		distance[origin.getNodeIndex()] = 0;
		previous[origin.getNodeIndex()] = origin.getNodeIndex();
		queue.add(new Label(origin, 0));

		// Process queue.
		while (settled.size() < numNodes && queue.size() > 0)
		{
			Label label = queue.poll();
			V node = label.getNode();
			if (settled.contains(node))
			{
				continue;
			}

			for (DirectedGraphArc<V, A> arc : graph.getOutArcs(node))
			{
				V to = arc.getTo();
				if (settled.contains(to))
				{
					continue;
				}
				double newDistance = distance[node.getNodeIndex()] + arc.getWeight();
				if (newDistance < distance[to.getNodeIndex()])
				{
					distance[to.getNodeIndex()] = newDistance;
					previous[to.getNodeIndex()] = node.getNodeIndex();
					previousArc[to.getNodeIndex()] = arc;

					// Add label to queue.
					queue.add(new Label(to, newDistance));
				}
			}
			settled.add(node);

			if (destination != null && destination.equals(node))
			{
				break;
			}
		}
	}

	public boolean containsPath(V to)
	{
		return distance[to.getNodeIndex()] < Double.MAX_VALUE;
	}

	public double getDistance(V to)
	{
		return distance[to.getNodeIndex()];
	}

	public List<DirectedGraphArc<V, A>> getPath(V to)
	{
		V currentNode = to;
		List<DirectedGraphArc<V, A>> path = new ArrayList<>();
		while (!currentNode.equals(origin))
		{
			int indexCurrent = currentNode.getNodeIndex();
			path.add(0, previousArc[indexCurrent]);
			V previousNode = graph.getNodes().get(previous[indexCurrent]);
			currentNode = previousNode;
		}
		return path;
	}

	private class Label
	{
		private final V node;
		private final double distance;

		public Label(V node, double distance)
		{
			this.node = node;
			this.distance = distance;
		}

		public V getNode()
		{
			return node;
		}

		public double getDistance()
		{
			return distance;
		}
	}

	public class LabelDistanceComparator implements Comparator<Label>
	{
		@Override
		public int compare(Label o1, Label o2)
		{
			return Double.compare(o1.getDistance(), o2.getDistance());
		}
	}
}
