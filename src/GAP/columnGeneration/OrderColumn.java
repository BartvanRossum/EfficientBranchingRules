package GAP.columnGeneration;

public class OrderColumn extends GAPColumn
{
	private final int index;
	private final int numAgents;

	public OrderColumn(int index, int numAgents)
	{
		super((double) (numAgents - 1 - 2 * index) / (numAgents - 1));

		this.index = index;
		this.numAgents = numAgents;
	}

	public boolean isMax()
	{
		return index == 0;
	}

	public boolean isMin()
	{
		return index == numAgents - 1;
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
