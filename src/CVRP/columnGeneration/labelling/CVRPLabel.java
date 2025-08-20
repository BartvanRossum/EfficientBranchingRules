package CVRP.columnGeneration.labelling;

import CVRP.instance.CustomerNode;
import graph.structures.digraph.DirectedGraphArc;

public class CVRPLabel
{
	private final int index;
	private final CVRPLabel previousLabel;
	private final DirectedGraphArc<CustomerNode, Integer> previousArc;
	private double cost;
	private final int memory;
	private final int distance;

	public CVRPLabel(int index, CVRPLabel previousLabel, DirectedGraphArc<CustomerNode, Integer> previousArc,
			double cost, int memory, int distance)
	{
		this.index = index;
		this.previousLabel = previousLabel;
		this.previousArc = previousArc;
		this.cost = cost;
		this.memory = memory;
		this.distance = distance;
	}

	public int getIndex()
	{
		return index;
	}

	public CVRPLabel getPreviousLabel()
	{
		return previousLabel;
	}

	public DirectedGraphArc<CustomerNode, Integer> getPreviousArc()
	{
		return previousArc;
	}

	public void addCost(double value)
	{
		cost += value;
	}

	public double getCost()
	{
		return cost;
	}

	public int getMemory()
	{
		return memory;
	}

	public int getDistance()
	{
		return distance;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(cost);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + distance;
		result = prime * result + index;
		result = prime * result + memory;
		result = prime * result + ((previousArc == null) ? 0 : previousArc.hashCode());
		result = prime * result + ((previousLabel == null) ? 0 : previousLabel.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CVRPLabel other = (CVRPLabel) obj;
		if (Double.doubleToLongBits(cost) != Double.doubleToLongBits(other.cost)) return false;
		if (distance != other.distance) return false;
		if (index != other.index) return false;
		if (memory != other.memory) return false;
		if (previousArc == null)
		{
			if (other.previousArc != null) return false;
		}
		else if (!previousArc.equals(other.previousArc)) return false;
		if (previousLabel == null)
		{
			if (other.previousLabel != null) return false;
		}
		else if (!previousLabel.equals(other.previousLabel)) return false;
		return true;
	}
}
