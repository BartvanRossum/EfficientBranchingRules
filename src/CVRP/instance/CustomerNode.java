package CVRP.instance;

import java.util.Set;

import graph.structures.digraph.DirectedGraphNodeIndex;

public class CustomerNode extends DirectedGraphNodeIndex
{
	private final int customer;
	private final int demand;
	private final Set<Integer> neighbours;
	private int bitwiseNeighbours;

	public CustomerNode(int customer, int demand, Set<Integer> neighbours)
	{
		this.customer = customer;
		this.demand = demand;
		this.neighbours = neighbours;
		
		this.bitwiseNeighbours = 0;
		for (int neighbour : neighbours)
		{
			bitwiseNeighbours += (1 << neighbour);
		}
	}

	public int getCustomer()
	{
		return customer;
	}

	public int getDemand()
	{
		return demand;
	}

	public Set<Integer> getNeighbours()
	{
		return neighbours;
	}
	
	public int getBitwiseNeighbours()
	{
		return bitwiseNeighbours;
	}

	public boolean isDepot()
	{
		return customer == 0;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + customer;
		result = prime * result + demand;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CustomerNode other = (CustomerNode) obj;
		if (customer != other.customer) return false;
		if (demand != other.demand) return false;
		return true;
	}
}
