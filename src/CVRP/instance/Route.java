package CVRP.instance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Route
{
	private final int vehicleIndex;
	private final int distance;
	private final int demand;

	private final List<Integer> nodes;

	public Route(int vehicleIndex, int distance, int demand, List<Integer> nodes)
	{
		this.vehicleIndex = vehicleIndex;
		this.distance = distance;
		this.demand = demand;
		this.nodes = nodes;
	}

	public Route(int vehicleIndex, List<Integer> nodes, CVRPInstance instance)
	{
		this.vehicleIndex = vehicleIndex;
		this.distance = instance.getDistance(nodes);
		this.demand = instance.getDemand(nodes);
		this.nodes = nodes;
	}

	public int getVehicleIndex()
	{
		return vehicleIndex;
	}

	public int getDistance()
	{
		return distance;
	}

	public int getDemand()
	{
		return demand;
	}

	public List<Integer> getNodes()
	{
		return nodes;
	}

	public static List<Route> readRoutes(String file, CVRPInstance instance) throws IOException
	{
		List<Route> routes = new ArrayList<>();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		String line = bufferedReader.readLine();
		while (line != null)
		{
			String[] data = line.split("\\s+");
			int index = Integer.valueOf(data[0]);
			int distance = Integer.valueOf(data[1]);
			int demand = Integer.valueOf(data[2]);
			
			List<Integer> nodes = new ArrayList<>();
			for (int i = 3; i < data.length; i++)
			{
				nodes.add(Integer.valueOf(data[i]));
			}
			
			routes.add(new Route(index, distance, demand, nodes));
			line = bufferedReader.readLine();
		}
		bufferedReader.close();
		return routes;
	}

	@Override
	public String toString()
	{
		String result = vehicleIndex + " " + distance + " " + demand + " ";
		for (Integer node : nodes)
		{
			result += node + " ";
		}
		return result;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + demand;
		result = prime * result + distance;
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		result = prime * result + vehicleIndex;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Route other = (Route) obj;
		if (demand != other.demand) return false;
		if (distance != other.distance) return false;
		if (nodes == null)
		{
			if (other.nodes != null) return false;
		}
		else if (!nodes.equals(other.nodes)) return false;
		if (vehicleIndex != other.vehicleIndex) return false;
		return true;
	}
}
