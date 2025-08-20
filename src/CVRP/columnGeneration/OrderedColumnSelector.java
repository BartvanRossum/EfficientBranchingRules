package CVRP.columnGeneration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import optimisation.columnGeneration.columnManagement.AbstractColumnSelector;
import util.Configuration;
import util.Pair;

public class OrderedColumnSelector extends AbstractColumnSelector<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	@Override
	public List<CVRPColumn> selectColumns(List<Pair<CVRPColumn, Double>> generatedColumns)
	{
		// Initialise list of duties.
		List<CVRPColumn> columns = new ArrayList<>();
		final int K = Configuration.getConfiguration().getIntProperty("K");
	
		// Sort columns.
		generatedColumns.sort(new ReducedCostComparator());
		for (int i = 0; i < Math.min(K, generatedColumns.size()); i++)
		{
			columns.add(generatedColumns.get(i).getKey());
		}
		return columns;
	}

	private class ReducedCostComparator implements Comparator<Pair<CVRPColumn, Double>>
	{
		@Override
		public int compare(Pair<CVRPColumn, Double> o1, Pair<CVRPColumn, Double> o2)
		{
			return Double.compare(o1.getValue(), o2.getValue());
		}
	}
}
