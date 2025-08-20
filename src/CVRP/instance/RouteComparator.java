package CVRP.instance;

import java.util.Comparator;

public class RouteComparator implements Comparator<Route>
{
	public int compare(Route a, Route b)
	{
		return getMinCustomer(a) - getMinCustomer(b);
	}

	private static int getMinCustomer(Route route)
	{
		int minIndex = Integer.MAX_VALUE;
		for (int i = 1; i < route.getNodes().size() - 1; i++)
		{
			minIndex = Math.min(minIndex, route.getNodes().get(i));
		}
		return minIndex;
	}
}
