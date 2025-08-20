package CVRP.columnGeneration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import CVRP.instance.Route;
import optimisation.columnGeneration.AbstractSolution;

public class CVRPSolution extends AbstractSolution<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	public CVRPSolution(double objectiveValue, Map<CVRPColumn, Double> columnMap)
	{
		super(objectiveValue, columnMap);
	}

	public List<Route> getRoutes()
	{
		List<Route> routes = new ArrayList<>();
		for (Entry<CVRPColumn, Double> entry : columnMap.entrySet())
		{
			if (entry.getKey() instanceof RouteColumn)
			{
				routes.add(((RouteColumn) entry.getKey()).getRoute());
			}
		}
		return routes;
	}
}
