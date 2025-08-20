package GAP.columnGeneration;

import GAP.GAPInstance;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import optimisation.columnGeneration.AbstractColumn;

public abstract class GAPColumn extends AbstractColumn<GAPInstance, GAPPricingProblem>
{
	public GAPColumn(double coefficient)
	{
		super(coefficient, false);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "GAPColumn []";
	}
}
