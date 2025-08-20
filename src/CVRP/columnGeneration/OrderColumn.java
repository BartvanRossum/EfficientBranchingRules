package CVRP.columnGeneration;

import CVRP.instance.CVRPConstants;

public class OrderColumn extends CVRPColumn
{
	private final int index;

	public OrderColumn(int index)
	{
		// The objective weight is given by w_i = (K - 1 - 2 i) / (K - 1).
		// Example for K = 2. w_0 = 1, w_1 = -1.
		// Example for K = 3. w_0 = 2, w_1 = 0, w_2 = -2.
		// Example for K = 4. w_0 = 3, w_1 = 1, w_2 = -1, w_3 = -3.
		super((double) (CVRPConstants.K - 1 - 2 * index) / (CVRPConstants.K - 1));

		this.index = index;
	}
	
	public OrderColumn(int index, double coefficient)
	{
		super(coefficient);
		
		this.index = index;
	}

	public boolean isMax()
	{
		return index == 0;
	}

	public boolean isMin()
	{
		return index == CVRPConstants.K - 1;
	}

	public int getIndex()
	{
		return index;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		OrderColumn other = (OrderColumn) obj;
		if (index != other.index) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "" + index;
	}
}
