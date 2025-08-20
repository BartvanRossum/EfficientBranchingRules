package CVRP.scripts;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import CVRP.columnGeneration.AllColumnSelector;
import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.CVRPMasterProblem;
import CVRP.columnGeneration.CVRPSolution;
import CVRP.columnGeneration.OrderColumn;
import CVRP.columnGeneration.RouteColumn;
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
import CVRP.models.TSP;
import ilog.concert.IloException;
import optimisation.BAP.BranchAndPrice;
import optimisation.BAP.NodeComparators.BoundComparator;
import optimisation.columnGeneration.ColumnGeneration;
import util.Configuration;
import util.Logger;
import util.Util;
import util.Writer;

public class MainGini
{
	public static void main(String[] args) throws IloException, IOException
	{
		// Parameter settings.
		int setting = Integer.valueOf(args[0]);
		int[] numCustomers =
		{ 15, 20, 25 };
		int K = 5;
		double alpha = 1.10;
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

		// To store solution.
		String solutionFile = "";
		StringBuilder sb = new StringBuilder();
		sb.append("numCustomers;alpha;t;LB;gini;giniTSP\n");

		// Iterate over the time horizon.
		for (int t = 0; t < T; t++)
		{
			String file = "dataCVRP/n" + N + "_k" + K + "_" + t + ".txt";
			System.out.println("Time: " + t);
			String settingsFile = "defaultCVRP.properties";
			Configuration.initialiseConfiguration(settingsFile, settingsFile);

			// Read instance.
			CVRPInstance instance = InstanceReader.readCVRPInstance(K, file);

			// Read in budget.
			List<Route> routes = Route.readRoutes("dataCVRP/solution_n" + N + "_k" + K + "_" + t + ".txt", instance);
			int budget = 0;
			int[] resources = new int[K];
			for (int i = 0; i < K; i++)
			{
				Route route = routes.get(i);
				resources[i] = CVRPConstants.RESOURCE_IS_DEMAND ? route.getDemand() : route.getDistance();
				budget += route.getDistance();
			}
			double upperBound = computeGini(resources);
			System.out.println("Upperbound Gini: " + upperBound);
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
				masterProblem.addColumn(new OrderColumn(i));
			}

			// Column generation settings.
			CVRPPricingRoutine pricingRoutine = new CVRPPricingRoutine();
			CVRPBucketSolver labelling = new CVRPBucketSolver();
			AllColumnSelector selector = new AllColumnSelector();
			ColumnGeneration<CVRPInstance, CVRPColumn, CVRPPricingProblem> columnGeneration = new ColumnGeneration<CVRPInstance, CVRPColumn, CVRPPricingProblem>(
					pricingRoutine, labelling, labelling, selector);

			// Branch-and-price settings.
			BranchAndPrice<CVRPInstance, CVRPColumn, CVRPPricingProblem> branchAndPrice = new BranchAndPrice<CVRPInstance, CVRPColumn, CVRPPricingProblem>(
					new BoundComparator<CVRPInstance, CVRPColumn, CVRPPricingProblem>(), instance, masterProblem,
					columnGeneration);
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

			// Check whether solutions are TSP.
			Map<CVRPColumn, Double> map = new LinkedHashMap<>();
			for (Route route : routes)
			{
				solutionFile += route.toString() + "\n";
				map.put(new RouteColumn(route), 1.0);
			}
			CVRPSolution solution = new CVRPSolution(upperBound, map);
			if (branchAndPrice.getBestSolution() != null)
			{
				solution = (CVRPSolution) branchAndPrice.getBestSolution();
			}
			double currentGini = solution.getObjectiveValue();
			int[] trueResources = new int[K];
			int routeIndex = 0;
			for (CVRPColumn column : solution.getColumnMap().keySet())
			{
				if (column instanceof RouteColumn)
				{
					RouteColumn routeColumn = (RouteColumn) column;
					if (solution.getColumnMap().get(column) < 0.1)
					{
						// Skip fractional columns.
						continue;
					}
					Route route = routeColumn.getRoute();
					int numStops = route.getNodes().size() - 1;
					double[][] distances = new double[numStops][numStops];
					for (int i = 0; i < route.getNodes().size() - 1; i++)
					{
						for (int j = 0; j < route.getNodes().size() - 1; j++)
						{
							distances[i][j] = instance.getDistances()[route.getNodes().get(i)][route.getNodes().get(j)];
						}
					}
					TSP tsp = new TSP(distances);
					tsp.solve();
					double distance = tsp.getObjectiveValue();
					trueResources[routeIndex] = (int) Math.rint(distance);
					routeIndex++;
					System.out.println("Route: " + route.getDistance() + ". TSP: " + distance);
					tsp.cleanup();
				}
			}
			double trueGini = computeGini(trueResources);
			System.out.println("True gini: " + trueGini + " vs. Estimated Gini: " + currentGini);

			sb.append(N + ";" + Writer.formatDouble(alpha) + ";" + t + ";"
					+ Writer.formatDouble(branchAndPrice.getLowerBound()) + ";" + Writer.formatDouble(currentGini) + ";"
					+ Writer.formatDouble(trueGini) + "\n");

			// Write logger.
			Writer.write(Logger.getLogger().getOutput(), "logger_CVRP_" + setting + ".csv");

			// Write solution.
			Writer.write(solutionFile, "solution_CVRP_" + setting + ".txt");
			Writer.write(sb.toString(), "TSP_CVRP_" + setting + ".csv");
		}
	}

	public static double computeGini(int[] utilities)
	{
		double gini = 0;
		Arrays.sort(utilities);
		int n = utilities.length;
		for (int i = 0; i < n; i++)
		{
			double weight = (double) (n - 1 - 2 * i) / (n - 1);
			gini += weight * utilities[n - i - 1];
		}
		return gini;
	}
}
