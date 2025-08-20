package GAP.columnGeneration;

import java.util.ArrayList;
import java.util.List;

import GAP.GAPInstance;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import optimisation.columnGeneration.columnManagement.AbstractColumnSelector;
import util.Pair;

public class AllColumnSelector extends AbstractColumnSelector<GAPInstance, GAPColumn, GAPPricingProblem>
{
	@Override
	public List<GAPColumn> selectColumns(List<Pair<GAPColumn, Double>> generatedColumns)
	{
		List<GAPColumn> columns = new ArrayList<>();
		for (Pair<GAPColumn, Double> pair : generatedColumns)
		{
			columns.add(pair.getKey());
		}
		return columns;
	}
}
