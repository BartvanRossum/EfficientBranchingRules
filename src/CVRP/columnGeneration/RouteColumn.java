package CVRP.columnGeneration;

import CVRP.instance.CVRPConstants;
import CVRP.instance.Route;

public class RouteColumn extends CVRPColumn
{
	private final Route route;

	public RouteColumn(Route route)
	{
		super(0);

		this.route = route;
	}

	public Route getRoute()
	{
		return route;
	}
	
	public int getPayoff()
	{
		if (CVRPConstants.RESOURCE_IS_DEMAND)
		{
			return route.getDemand();
		}
		return route.getDistance();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((route == null) ? 0 : route.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		RouteColumn other = (RouteColumn) obj;
		if (route == null)
		{
			if (other.route != null) return false;
		}
		else if (!route.equals(other.route)) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return route.toString();
	}
}
