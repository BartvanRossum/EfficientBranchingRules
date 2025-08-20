package optimisation.BAP.NodeComparators;

import java.util.Comparator;

import optimisation.BAP.BAPNode;
import optimisation.columnGeneration.AbstractColumn;
import optimisation.columnGeneration.AbstractInstance;
import optimisation.columnGeneration.pricing.AbstractPricingProblem;

public class DynamicComparator<T extends AbstractInstance, U extends AbstractColumn<T, V>, V extends AbstractPricingProblem<T>>
		implements Comparator<BAPNode<T, U, V>>
{
	private boolean hasSwitched = false;

	@Override
	public int compare(BAPNode<T, U, V> o1, BAPNode<T, U, V> o2)
	{
		if (hasSwitched)
		{
			if (o1.getLowerBound() == o2.getLowerBound())
			{
				return o2.getDepth() - o1.getDepth();
			}
			return Double.compare(o1.getLowerBound(), o2.getLowerBound());
		}
		else
		{
			if (o2.getDepth() == o1.getDepth())
			{
				return Double.compare(o1.getLowerBound(), o2.getLowerBound());
			}
			return o2.getDepth() - o1.getDepth();
		}
	}

	public void performSwitch()
	{
		hasSwitched = true;
	}
}
