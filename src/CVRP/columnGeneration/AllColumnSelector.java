package CVRP.columnGeneration;

import java.util.ArrayList;
import java.util.List;

import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import optimisation.columnGeneration.columnManagement.AbstractColumnSelector;
import util.Pair;

public class AllColumnSelector extends AbstractColumnSelector<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	@Override
	public List<CVRPColumn> selectColumns(List<Pair<CVRPColumn, Double>> generatedColumns)
	{
		List<CVRPColumn> columns = new ArrayList<>();
		for (Pair<CVRPColumn, Double> pair : generatedColumns)
		{
			columns.add(pair.getKey());
		}
		return columns;
	}
}
