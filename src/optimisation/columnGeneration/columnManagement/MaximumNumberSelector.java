package optimisation.columnGeneration.columnManagement;

import java.util.ArrayList;
import java.util.List;

import optimisation.columnGeneration.AbstractColumn;
import optimisation.columnGeneration.AbstractInstance;
import optimisation.columnGeneration.pricing.AbstractPricingProblem;
import util.Pair;

public class MaximumNumberSelector<T extends AbstractInstance, U extends AbstractColumn<T, V>, V extends AbstractPricingProblem<T>> extends AbstractColumnSelector<T, U, V>
{
	private final int maximumNumberColumns;
	
	public MaximumNumberSelector(int maximumNumberColumns)
	{
		this.maximumNumberColumns = maximumNumberColumns;
	}
	
	@Override
	public List<U> selectColumns(List<Pair<U, Double>> generatedColumns)
	{
		List<U> columns = new ArrayList<>();
		if (generatedColumns.size() > maximumNumberColumns)
		{
			generatedColumns.sort(new ReducedCostComparator<>());
			generatedColumns = generatedColumns.subList(0, maximumNumberColumns);
		}
		for (Pair<U, Double> pair : generatedColumns)
		{
			columns.add(pair.getKey());
		}
		return columns;
	}
	
}
