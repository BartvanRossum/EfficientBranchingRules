package CVRP.columnGeneration.branching;

import java.util.LinkedHashSet;
import java.util.Set;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import optimisation.BAP.AbstractBranchingDecision;
import optimisation.columnGeneration.AbstractConstraint;

public class BranchingDecisionVehicleCustomer
		extends AbstractBranchingDecision<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final boolean isAllowed;
	private final int vehicleIndex;
	private final int customer;

	public BranchingDecisionVehicleCustomer(boolean isAllowed, int vehicleIndex, int customer)
	{
		this.isAllowed = isAllowed;
		this.vehicleIndex = vehicleIndex;
		this.customer = customer;
	}

	@Override
	public boolean isCompatible(CVRPPricingProblem pricingProblem)
	{
		return true;
	}

	@Override
	public void modifyPricingProblem(CVRPPricingProblem pricingProblem)
	{
		// Deactivate node.
		if ((isAllowed && pricingProblem.getVehicleIndex() != vehicleIndex)
				|| (!isAllowed && pricingProblem.getVehicleIndex() == vehicleIndex))
		{
			pricingProblem.addForbiddenNode(customer);
		}
	}

	@Override
	public Set<AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>> getBranchingConstraints()
	{
		Set<AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem>> constraints = new LinkedHashSet<>();
		constraints.add(new BranchingConstraintVehicleCustomer(isAllowed, vehicleIndex, customer));
		return constraints;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + customer;
		result = prime * result + (isAllowed ? 1231 : 1237);
		result = prime * result + vehicleIndex;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		BranchingDecisionVehicleCustomer other = (BranchingDecisionVehicleCustomer) obj;
		if (customer != other.customer) return false;
		if (isAllowed != other.isAllowed) return false;
		if (vehicleIndex != other.vehicleIndex) return false;
		return true;
	}
}