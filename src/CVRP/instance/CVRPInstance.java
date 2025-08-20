package CVRP.instance;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import graph.structures.digraph.DirectedGraph;
import optimisation.columnGeneration.AbstractInstance;

public class CVRPInstance extends AbstractInstance
{
	private final int N;
	private final int K;
	private final int Q;
	private final int[] demands;
	private final int[][] distances;

	private final DirectedGraph<CustomerNode, Integer> graph;

	public CVRPInstance(int N, int K, int Q, int[] demands, int[][] distances)
	{
		this.N = N;
		this.K = K;
		this.Q = Q;
		CVRPConstants.N = N;
		CVRPConstants.K = K;
		CVRPConstants.Q = Q;
		this.demands = demands;
		this.distances = distances;

		this.graph = new DirectedGraph<>();
		for (int i = 0; i <= N; i++)
		{
			graph.addNode(new CustomerNode(i, i > 0 ? demands[i - 1] : 0, computeNeighbours(i)));
		}
		for (int i = 0; i <= N; i++)
		{
			for (int j = 0; j <= N; j++)
			{
				if (distances[i][j] > 0)
				{
					CustomerNode from = graph.getNodes().get(i);
					CustomerNode to = graph.getNodes().get(j);
					graph.addArc(from, to, distances[i][j], distances[i][j]);
				}
			}
		}
	}

	private Set<Integer> computeNeighbours(int customer)
	{
		Set<Integer> neighbours = new LinkedHashSet<>();
		if (customer == 0)
		{
			neighbours.add(customer);
			return neighbours;
		}
		int[] intArray = IntStream	.range(1, N + 1).boxed()
									.sorted(Comparator.comparing(i -> distances[customer][i]))
									.mapToInt(Integer::intValue).toArray();
		for (int i = 0; i <= CVRPConstants.NG_NEIGHBOURHOOD_SIZE; i++)
		{
			neighbours.add(intArray[i]);
		}
		return neighbours;
	}

	public DirectedGraph<CustomerNode, Integer> getGraph()
	{
		return graph;
	}

	public int getN()
	{
		return N;
	}

	public int getK()
	{
		return K;
	}

	public int getQ()
	{
		return Q;
	}

	public int getDistance(List<Integer> nodes)
	{
		int distance = 0;
		for (int i = 0; i < nodes.size() - 1; i++)
		{
			distance += distances[nodes.get(i)][nodes.get(i + 1)];
		}
		return distance;
	}

	public int getDemand(List<Integer> nodes)
	{
		int demand = 0;
		for (int i = 1; i < nodes.size() - 1; i++)
		{
			demand += demands[nodes.get(i) - 1];
		}
		return demand;
	}

	@Override
	public String toString()
	{
		return "Instance [N=" + N + ", K=" + K + ", Q=" + Q + ", demands=" + Arrays.toString(demands) + ", distances="
				+ Arrays.toString(distances) + "]";
	}

	public int[] getDemands()
	{
		return demands;
	}

	public int[][] getDistances()
	{
		return distances;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Q;
		result = prime * result + Arrays.hashCode(demands);
		result = prime * result + Arrays.deepHashCode(distances);
		result = prime * result + K;
		result = prime * result + N;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CVRPInstance other = (CVRPInstance) obj;
		if (Q != other.Q) return false;
		if (!Arrays.equals(demands, other.demands)) return false;
		if (!Arrays.deepEquals(distances, other.distances)) return false;
		if (K != other.K) return false;
		if (N != other.N) return false;
		return true;
	}
}
