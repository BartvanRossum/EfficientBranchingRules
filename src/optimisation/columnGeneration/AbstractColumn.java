package optimisation.columnGeneration;

import optimisation.columnGeneration.pricing.AbstractPricingProblem;

public abstract class AbstractColumn<T extends AbstractInstance, V extends AbstractPricingProblem<T>>
{
	private final double coefficient;
	private final boolean isAuxiliaryColumn;
	private int numIterUnused = 0;

	public AbstractColumn(double coefficient, boolean isAuxiliaryColumn)
	{
		this.coefficient = coefficient;
		this.isAuxiliaryColumn = isAuxiliaryColumn;
	}
	
	public double getCoefficient()
	{
		return coefficient;
	}

	public boolean isAuxiliaryColumn()
	{
		return isAuxiliaryColumn;
	}

	public int getNumIterUnused()
	{
		return numIterUnused;
	}

	public void increaseNumIterUnused()
	{
		numIterUnused++;
	}

	public void resetNumIterUnused()
	{
		numIterUnused = 0;
	}

	public abstract boolean equals(Object o);

	public abstract int hashCode();

	public abstract String toString();
}
