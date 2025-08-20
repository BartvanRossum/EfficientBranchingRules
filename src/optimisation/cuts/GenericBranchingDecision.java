package optimisation.cuts;

import java.util.LinkedHashSet;
import java.util.Set;

import optimisation.BAP.AbstractBranchingDecision;
import optimisation.columnGeneration.AbstractColumn;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractInstance;
import optimisation.columnGeneration.pricing.AbstractPricingProblem;

public class GenericBranchingDecision<T extends AbstractInstance, U extends AbstractColumn<T, V>, V extends AbstractPricingProblem<T>> extends AbstractBranchingDecision<T, U, V>
{
	private final Set<AbstractConstraint<T, U, V>> constraints;
	
	public GenericBranchingDecision(AbstractConstraint<T, U, V> constraint)
	{
		this.constraints = new LinkedHashSet<>();
		constraints.add(constraint);
	}
	@Override
	public Set<AbstractConstraint<T, U, V>> getBranchingConstraints()
	{
		return constraints;
	}

	@Override
	public boolean isCompatible(V pricingProblem)
	{
		return true;
	}

	@Override
	public void modifyPricingProblem(V pricingProblem)
	{
		// Do nothing.
	}
}
