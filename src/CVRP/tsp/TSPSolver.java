package CVRP.tsp;

import java.io.IOException;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.CVRPMasterProblem;
import CVRP.columnGeneration.CVRPSolution;
import CVRP.columnGeneration.OrderColumn;
import CVRP.columnGeneration.OrderedColumnSelector;
import CVRP.columnGeneration.branching.BranchingRuleArc;
import CVRP.columnGeneration.branching.BranchingRuleLastCustomer;
import CVRP.columnGeneration.branching.BranchingRuleRange;
import CVRP.columnGeneration.branching.BranchingRuleVehicleCustomer;
import CVRP.columnGeneration.constraints.AssignmentConstraint;
import CVRP.columnGeneration.constraints.BudgetConstraint;
import CVRP.columnGeneration.constraints.CardinalityConstraint;
import CVRP.columnGeneration.constraints.IndexMaxConstraint;
import CVRP.columnGeneration.constraints.IndexMinConstraint;
import CVRP.columnGeneration.constraints.LowerBoundConstraint;
import CVRP.columnGeneration.constraints.MaxConstraint;
import CVRP.columnGeneration.constraints.MinConstraint;
import CVRP.columnGeneration.constraints.OrderConstraint;
import CVRP.columnGeneration.constraints.PartitionConstraint;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.columnGeneration.pricing.CVRPPricingRoutine;
import CVRP.instance.CVRPConstants;
import CVRP.instance.CVRPInstance;
import ilog.concert.IloException;
import optimisation.BAP.BranchAndPrice;
import optimisation.BAP.NodeComparators.BoundComparator;
import optimisation.columnGeneration.ColumnGeneration;

public class TSPSolver
{
	public static CVRPSolution run(CVRPInstance instance, int budget, int alphaBudget, double upperBound,
			int[] utilities, long timeLimit, boolean efficientBranching) throws IloException, IOException
	{
		// Initialise master.
		CVRPMasterProblem masterProblem = new CVRPMasterProblem(instance);
		masterProblem.setOut(null);

		// Add partition constraints.
		for (int i = 1; i <= instance.getN(); i++)
		{
			masterProblem.addConstraint(new PartitionConstraint(i));
		} 

		// Add budget constraint.
		masterProblem.addConstraint(new BudgetConstraint(alphaBudget));

		// Add a lower bound on the maximum route.
		int upperBoundMin = (int) Math.floor(alphaBudget / instance.getK());
		int lowerBoundMax = (int) Math.floor(budget / instance.getK());
		if (CVRPConstants.RESOURCE_IS_DEMAND)
		{
			int totalDemand = 0;
			for (int demand : instance.getDemands())
			{
				totalDemand += demand;
			}
			lowerBoundMax = (int) Math.floor(totalDemand / instance.getK());
			upperBoundMin = lowerBoundMax;
		}
		masterProblem.addConstraint(new LowerBoundConstraint(lowerBoundMax));

		// Add constraints depending on formulation.
		switch (CVRPConstants.FORMULATION)
		{
			case "VEHICLE_INDEX":
				// Add assignment constraints.
				for (int k = 0; k < instance.getK(); k++)
				{
					masterProblem.addConstraint(new AssignmentConstraint(k));
				}

				// Add max/min constraints for each vehicle.
				for (int k = 0; k < instance.getK(); k++)
				{
					masterProblem.addConstraint(new IndexMaxConstraint(k, utilities[k]));
					masterProblem.addConstraint(new IndexMinConstraint(k, utilities[k]));
				}
				break;
			case "LAST_CUSTOMER":
				for (int i = 1; i <= instance.getN(); i++)
				{
					masterProblem.addConstraint(new MinConstraint(i, upperBoundMin));
					masterProblem.addConstraint(new MaxConstraint(i));
				}

				// Add cardinality constraints.
				masterProblem.addConstraint(new CardinalityConstraint(instance.getK()));
				break;
			case "NO_CONSTRAINTS":
				// We do not explicitly model the minimum and maximum here.
				// Add cardinality constraints.
				masterProblem.addConstraint(new CardinalityConstraint(instance.getK()));
				break;
			default:
				break;
		}

		// Add constraint stating that min <= max.
		masterProblem.addConstraint(new OrderConstraint());

		// Add fairness columns.
		masterProblem.addColumn(new OrderColumn(0)); 
		masterProblem.addColumn(new OrderColumn(CVRPConstants.K - 1));

		// Column generation settings.
		CVRPPricingRoutine pricingRoutine = new CVRPPricingRoutine();
		TSPBucketSolver labelling = new TSPBucketSolver();
		OrderedColumnSelector selector = new OrderedColumnSelector();
		ColumnGeneration<CVRPInstance, CVRPColumn, CVRPPricingProblem> columnGeneration = new ColumnGeneration<CVRPInstance, CVRPColumn, CVRPPricingProblem>(
				pricingRoutine, labelling, labelling, selector);

		// Branch-and-price settings.
		BranchAndPrice<CVRPInstance, CVRPColumn, CVRPPricingProblem> branchAndPrice = new BranchAndPrice<CVRPInstance, CVRPColumn, CVRPPricingProblem>(
				new BoundComparator<CVRPInstance, CVRPColumn, CVRPPricingProblem>(),
				instance, masterProblem, columnGeneration);
		branchAndPrice.setUpperBound(upperBound);
		branchAndPrice.setTimeLimit(timeLimit);

		// Branching rules.
		switch (CVRPConstants.FORMULATION)
		{
			case "VEHICLE_INDEX":
				branchAndPrice.addBranchingRule(new BranchingRuleVehicleCustomer(0));
				break;
			case "LAST_CUSTOMER":
				branchAndPrice.addBranchingRule(new BranchingRuleLastCustomer(-1));
				break;
			case "NO_CONSTRAINTS":
				branchAndPrice.addBranchingRule(new BranchingRuleRange(-1, 1.0));
			default:
				break;
		}
		if (efficientBranching)
		{
			branchAndPrice.addBranchingRule(new BranchingRuleRange(-2, 0.975));
		}
		branchAndPrice.addBranchingRule(new BranchingRuleArc(1));

		// Solve with branch-and-price.
		branchAndPrice.applyBranchAndPrice();

		// Return solution.
		return (CVRPSolution) branchAndPrice.getBestSolution();
	}
}