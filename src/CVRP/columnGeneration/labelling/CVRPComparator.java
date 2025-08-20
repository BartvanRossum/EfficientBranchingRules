package CVRP.columnGeneration.labelling;

import java.util.Comparator;

public class CVRPComparator implements Comparator<CVRPLabel>
{
	@Override
	public int compare(CVRPLabel o1, CVRPLabel o2)
	{
		// First compare based on reduced cost.
		if (o1.getCost() != o2.getCost())
		{
			return Double.compare(o1.getCost(), o2.getCost());
		}

		// If identical up to now, order by index.
		return Integer.compare(o1.getIndex(), o2.getIndex());
	}
}
