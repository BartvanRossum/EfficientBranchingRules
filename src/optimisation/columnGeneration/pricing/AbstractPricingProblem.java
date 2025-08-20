package optimisation.columnGeneration.pricing;

import optimisation.columnGeneration.AbstractInstance;

public abstract class AbstractPricingProblem<T extends AbstractInstance>
{
	public void makeThreadSafe()
	{
		// Do nothing. This is an auxiliary method that can be used for multithreading purposes.
	}
}
