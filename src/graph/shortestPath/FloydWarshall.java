package graph.shortestPath;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import graph.structures.digraph.DirectedGraph;
import graph.structures.digraph.DirectedGraphArc;
import graph.structures.digraph.DirectedGraphNodeIndex;
import graph.structures.digraph.activation.Activation;

public class FloydWarshall<V extends DirectedGraphNodeIndex, A>
{
	private final DirectedGraph<V, A> graph;
	private final int numNodes;
	private double[][] distance;
	private int[][] next;
	private DirectedGraphArc<V, A>[][] nextArc;

	public FloydWarshall(DirectedGraph<V, A> graph)
	{
		this.graph = graph;
		graph.setNodeIndices();
		this.numNodes = graph.getNumberOfNodes();
	}

	@SuppressWarnings("unchecked")
	public void computeDistances()
	{
		// Initialise distance matrix.
		this.distance = new double[numNodes][numNodes];
		for (int i = 0; i < numNodes; i++)
		{
			for (int j = 0; j < numNodes; j++)
			{
				distance[i][j] = (i == j) ? 0 : Double.MAX_VALUE;
			}
		}

		// Initialise successor matrix.
		this.next = new int[numNodes][numNodes];
		this.nextArc = (DirectedGraphArc<V, A>[][]) Array.newInstance(DirectedGraphArc.class, numNodes, numNodes);

		// Process each edge.
		for (DirectedGraphArc<V, A> arc : graph.getArcs())
		{
			if (Activation.getActivation().getActivationFunction().isActiveArc(arc))
			{
				int from = arc.getFrom().getNodeIndex();
				int to = arc.getTo().getNodeIndex();
				if (arc.getWeight() < distance[from][to])
				{
					distance[from][to] = arc.getWeight();
					next[from][to] = to;
					nextArc[from][to] = arc;
				}
			}
		}

		// Perform updating iterations.
		for (int k = 0; k < numNodes; k++)
		{
			for (int i = 0; i < numNodes; i++)
			{
				// Skip iterations that cannot lead to an improvement.
				if (distance[i][k] == Double.MAX_VALUE)
				{
					continue;
				}
				for (int j = 0; j < numNodes; j++)
				{
					if (distance[i][j] > distance[i][k] + distance[k][j])
					{
						distance[i][j] = distance[i][k] + distance[k][j];
						next[i][j] = next[i][k];
						nextArc[i][j] = nextArc[i][k];
					}
				}
			}
		}
	}

	public boolean containsPath(V from, V to)
	{
		return (getDistance(from, to) < Double.MAX_VALUE);
	}

	public double getDistance(V from, V to)
	{
		return distance[from.getNodeIndex()][to.getNodeIndex()];
	}

	public List<DirectedGraphArc<V, A>> getPath(V from, V to)
	{
		V currentNode = from;
		int indexTo = to.getNodeIndex();
		List<DirectedGraphArc<V, A>> path = new ArrayList<>();
		while (!currentNode.equals(to))
		{
			int indexCurrent = currentNode.getNodeIndex();
			V nextNode = graph.getNodes().get(next[indexCurrent][indexTo]);
			path.add(nextArc[indexCurrent][indexTo]);
			currentNode = nextNode;
		}
		return path;
	}
}
