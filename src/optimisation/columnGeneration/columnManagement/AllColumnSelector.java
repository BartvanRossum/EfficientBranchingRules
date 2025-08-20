package optimisation.columnGeneration.columnManagement;

import java.util.ArrayList;
import java.util.List;

import optimisation.columnGeneration.AbstractColumn;
import optimisation.columnGeneration.AbstractInstance;
import optimisation.columnGeneration.pricing.AbstractPricingProblem;
import util.Pair;

public class AllColumnSelector<T extends AbstractInstance, U extends AbstractColumn<T, V>, V extends AbstractPricingProblem<T>>
		extends AbstractColumnSelector<T, U, V>
{
	@Override
	public List<U> selectColumns(List<Pair<U, Double>> generatedColumns)
	{
		List<U> columns = new ArrayList<>();
		for (Pair<U, Double> pair : generatedColumns)
		{
			columns.add(pair.getKey());
		}
		return columns;
	}
}
