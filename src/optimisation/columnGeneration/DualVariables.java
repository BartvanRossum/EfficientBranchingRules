package optimisation.columnGeneration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import optimisation.columnGeneration.pricing.AbstractPricingProblem;

public class DualVariables<T extends AbstractInstance, U extends AbstractColumn<T, V>, V extends AbstractPricingProblem<T>>
{
	private final Map<AbstractConstraint<T, U, V>, Double> dualMap;
	
	public DualVariables()
	{
		this.dualMap = new LinkedHashMap<>();
	}
	
	public void set(AbstractConstraint<T, U, V> constraint, double dual)
	{
		dualMap.put(constraint, dual);
	}
	
	public double get(AbstractConstraint<T, U, V> constraint)
	{
		return dualMap.get(constraint);
	}
	
	public boolean contains(AbstractConstraint<T, U, V> constraint)
	{
		return dualMap.containsKey(constraint);
	}
	
	public Set<AbstractConstraint<T, U, V>> getConstraints()
	{
		return dualMap.keySet();
	}
	
	public DualVariables<T, U, V> getCopy()
	{
		DualVariables<T, U, V> copy =new DualVariables<>();
		for (Entry<AbstractConstraint<T, U, V>, Double> entry : dualMap.entrySet())
		{
			copy.set(entry.getKey(), entry.getValue());
		}
		return copy;
	}
}
