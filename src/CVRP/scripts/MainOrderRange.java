package CVRP.scripts;

import java.io.IOException;
import java.util.List;

import CVRP.columnGeneration.AllColumnSelector;
import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.CVRPMasterProblem;
import CVRP.columnGeneration.OrderColumn;
import CVRP.columnGeneration.branching.BranchingRuleArc;
import CVRP.columnGeneration.branching.BranchingRuleOrder;
import CVRP.columnGeneration.branching.BranchingRuleVehicleCustomer;
import CVRP.columnGeneration.constraints.AssignmentConstraint;
import CVRP.columnGeneration.constraints.BudgetConstraint;
import CVRP.columnGeneration.constraints.PartitionConstraint;
import CVRP.columnGeneration.constraints.RankOrderConstraint;
import CVRP.columnGeneration.constraints.VehicleOrderConstraint;
import CVRP.columnGeneration.labelling.CVRPBucketSolver;
import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.columnGeneration.pricing.CVRPPricingRoutine;
import CVRP.data.InstanceReader;
import CVRP.instance.CVRPConstants;
import CVRP.instance.CVRPInstance;
import CVRP.instance.Route;
import ilog.concert.IloException;
import optimisation.BAP.BranchAndPrice;
import optimisation.BAP.NodeComparators.BoundComparator;
import optimisation.columnGeneration.ColumnGeneration;
import util.Configuration;
import util.Logger;
import util.Util;
import util.Writer;

public class MainOrderRange
{
	public static void main(String[] args) throws IloException, IOException
	{
		// Parameter settings.
		int setting = Integer.valueOf(args[0]);
		int[] numCustomers =
		{ 15, 20, 25 };
		int K = 5;
		double alpha = 1.1;
		int T = 20;
		boolean demandResource = false;
		boolean[] rangeBranchings =
		{ false, true };
		long timeLimit = 1 * 60 * 60 * 1000;

		int[] sizes =
		{ 3, 2 };
		int[] indices = Util.retrieveSettings(sizes, setting);
		int N = numCustomers[indices[0]];
		boolean rangeBranching = rangeBranchings[indices[1]];
		String formulation = "VEHICLE_INDEX";
		System.out.println(formulation + " " + rangeBranching + " " + N);

		// Choose the formulation. Note that we must turn off symmetry-breaking.
		CVRPConstants.FORMULATION = formulation;
		CVRPConstants.RESOURCE_IS_DEMAND = demandResource;
		CVRPConstants.USE_U = true;

		for (int t = 0; t < T; t++)
		{
			// Iterate over the time horizon.
			String file = "dataCVRP/n" + N + "_k" + K + "_" + t + ".txt";
			System.out.println("Time: " + t);
			String settingsFile = "defaultCVRP.properties";
			Configuration.initialiseConfiguration(settingsFile, settingsFile);

			// Read instance.
			CVRPInstance instance = InstanceReader.readCVRPInstance(K, file);

			// Read in budget.
			List<Route> routes = Route.readRoutes("dataCVRP/solution_n" + N + "_k" + K + "_" + t + ".txt",
					instance);
			int budget = 0;
			int[] resources = new int[K];
			int max = 0;
			int min = Integer.MAX_VALUE;
			for (int i = 0; i < K; i++)
			{
				Route route = routes.get(i);
				resources[i] = CVRPConstants.RESOURCE_IS_DEMAND ? route.getDemand() : route.getDistance();
				budget += route.getDistance();
				max = Math.max(max, resources[i]);
				min = Math.min(min, resources[i]);
			}
			double upperBound = max - min;
			System.out.println("Upperbound range: " + upperBound);
			int alphaBudget = (int) Math.floor(alpha * budget);

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

			// Add assignment constraints and order constraints for each vehicle.
			for (int k = 0; k < instance.getK(); k++)
			{
				masterProblem.addConstraint(new AssignmentConstraint(k));
				masterProblem.addConstraint(new VehicleOrderConstraint(k));
			}

			// Add constraints representing order among vehicles.
			for (int k = 0; k < instance.getK() - 1; k++)
			{
				masterProblem.addConstraint(new RankOrderConstraint(k));
			}

			// Add order statistic columns.
			for (int i = 0; i < K; i++)
			{
				double coefficient = 0;
				if (i == 0)
				{
					coefficient = 1;
				}
				if (i == K - 1)
				{
					coefficient = -1;
				}
				masterProblem.addColumn(new OrderColumn(i, coefficient));
			}

			// Column generation settings.
			CVRPPricingRoutine pricingRoutine = new CVRPPricingRoutine();
			CVRPBucketSolver labelling = new CVRPBucketSolver();
			AllColumnSelector selector = new AllColumnSelector();
			ColumnGeneration<CVRPInstance, CVRPColumn, CVRPPricingProblem> columnGeneration = new ColumnGeneration<CVRPInstance, CVRPColumn, CVRPPricingProblem>(
					pricingRoutine, labelling, labelling, selector);

			// Branch-and-price settings.
			BranchAndPrice<CVRPInstance, CVRPColumn, CVRPPricingProblem> branchAndPrice = new BranchAndPrice<CVRPInstance, CVRPColumn, CVRPPricingProblem>(
					new BoundComparator<CVRPInstance, CVRPColumn, CVRPPricingProblem>(),
					instance, masterProblem, columnGeneration);
			branchAndPrice.setUpperBound(upperBound);
			branchAndPrice.setTimeLimit(timeLimit);

			// Branching rules.
			branchAndPrice.addBranchingRule(new BranchingRuleVehicleCustomer(0));
			if (rangeBranching)
			{
				branchAndPrice.addBranchingRule(new BranchingRuleOrder(-2, 0.975));
			}
			branchAndPrice.addBranchingRule(new BranchingRuleArc(1));

			// Solve with branch-and-price.
			branchAndPrice.applyBranchAndPrice();

			// Write logger.
			Writer.write(Logger.getLogger().getOutput(), "logger_CVRP_" + setting + ".csv");
		}
	}
}
