package optimisation.cuts;

import java.util.Set;

import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractColumn;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractInstance;
import optimisation.columnGeneration.AbstractMasterProblem;
import optimisation.columnGeneration.pricing.AbstractPricingProblem;

public abstract class AbstractCutSeparator<T extends AbstractInstance, U extends AbstractColumn<T, V>, V extends AbstractPricingProblem<T>>
{
	public final double priority;
	private final boolean isGloballyValid;

	public AbstractCutSeparator(double priority, boolean isGloballyValid)
	{
		this.priority = priority;
		this.isGloballyValid = isGloballyValid;
	} 
	
	public abstract boolean separate(int nodeIndex, boolean enumerating);
	
	public boolean isGloballyValid()
	{
		return isGloballyValid;
	}

	public abstract Set<AbstractConstraint<T, U, V>> generateCuts(AbstractMasterProblem<T, U, V> masterProblem)
			throws IloException;
}
