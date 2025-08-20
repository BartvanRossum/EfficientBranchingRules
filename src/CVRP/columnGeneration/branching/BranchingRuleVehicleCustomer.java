package CVRP.columnGeneration.branching;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.RouteColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import optimisation.BAP.AbstractBranchingRule;
import optimisation.BAP.BAPNode;
import optimisation.BAP.BranchingCandidate;
import util.Configuration;

public class BranchingRuleVehicleCustomer extends AbstractBranchingRule<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	public BranchingRuleVehicleCustomer(double priority)
	{
		super(priority);
	}

	@Override
	public List<BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem>> getBranchingCandidates(
			BAPNode<CVRPInstance, CVRPColumn, CVRPPricingProblem> parent)
	{
		// Compute value per combination of vehicle and customer.
		Map<Integer, Map<Integer, Double>> customerValueMap = new LinkedHashMap<>();
		for (Entry<CVRPColumn, Double> entry : parent.getSolution().getColumnMap().entrySet())
		{
			if (!(entry.getKey() instanceof RouteColumn))
			{
				continue;
			}
			RouteColumn routeColumn = (RouteColumn) entry.getKey();
			double value = entry.getValue();
			int vehicleIndex = routeColumn.getRoute().getVehicleIndex();
			if (!customerValueMap.containsKey(vehicleIndex))
			{
				customerValueMap.put(vehicleIndex, new LinkedHashMap<>());
			}
			List<Integer> nodes = routeColumn.getRoute().getNodes();
			for (int i = 1; i < nodes.size() - 1; i++)
			{
				int customer = nodes.get(i);
				if (!customerValueMap.get(vehicleIndex).containsKey(customer))
				{
					customerValueMap.get(vehicleIndex).put(customer, 0.0);
				}
				customerValueMap.get(vehicleIndex).put(customer,
						customerValueMap.get(vehicleIndex).get(customer) + value);
			}
		}

		// Generate branching candidates.
		List<BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem>> candidates = new ArrayList<>();
		double branchingPrecision = Configuration.getConfiguration().getDoubleProperty("BRANCHING_PRECISION");
		for (int vehicleIndex : customerValueMap.keySet())
		{
			for (Entry<Integer, Double> entry : customerValueMap.get(vehicleIndex).entrySet())
			{
				double value = entry.getValue();
				double fractionalValue = Math.abs(value - Math.rint(value));
				if (fractionalValue > branchingPrecision)
				{
					BranchingCandidate<CVRPInstance, CVRPColumn, CVRPPricingProblem> candidate = new BranchingCandidate<>(
							fractionalValue);
					candidate.addBranchingDecision(
							new BranchingDecisionVehicleCustomer(true, vehicleIndex, entry.getKey()));
					candidate.addBranchingDecision(
							new BranchingDecisionVehicleCustomer(false, vehicleIndex, entry.getKey()));
					candidates.add(candidate);
				}
			}
		}
		return candidates;
	}
}
