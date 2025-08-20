package CVRP.scripts;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import CVRP.columnGeneration.CVRPColumn;
import CVRP.columnGeneration.CVRPSolution;
import CVRP.columnGeneration.RouteColumn;
import CVRP.columnGeneration.Solver;
import CVRP.data.InstanceReader;
import CVRP.instance.CVRPConstants;
import CVRP.instance.CVRPInstance;
import CVRP.instance.Route;
import CVRP.models.TSP;
import ilog.concert.IloException;
import util.Configuration;
import util.Logger;
import util.Util;
import util.Writer;

public class MainRangeBranchingTSP
{
	public static void main(String[] args) throws IloException, IOException
	{
		// Parameter settings.
		int[] numCustomers =
		{ 15, 20, 25 };
		int K = 5;
		double[] alphas =
		{ 1.01, 1.05, 1.1 };
		int T = 20;
		boolean demandResource = false;
		boolean rangeBranching = true;
		String formulation = "LAST_CUSTOMER";
		long timeLimit = 1 * 60 * 60 * 1000;

		int setting = Integer.valueOf(args[0]);
		int[] sizes =
		{ 3, 3 }; 
		int[] indices = Util.retrieveSettings(sizes, setting);
		int N = numCustomers[indices[0]];
		double alpha = alphas[indices[1]];
		System.out.println(N + " " + formulation + " " + rangeBranching);

		// Choose the formulation.
		CVRPConstants.FORMULATION = formulation;
		CVRPConstants.RESOURCE_IS_DEMAND = demandResource;

		// Initialise a file to keep track of the TSP-optimality of routes.
		StringBuilder sb = new StringBuilder();
		sb.append("numCustomers;alpha;t;range;rangeTSP\n");
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
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			List<Route> routes = Route.readRoutes("dataCVRP/solution_n" + N + "_k" + K + "_" + t + ".txt",
					instance);
			Map<CVRPColumn, Double> map = new LinkedHashMap<>();
			for (Route route : routes)
			{
				map.put(new RouteColumn(route), 1.0);
			}

			int budget = 0;
			for (Route route : routes)
			{
				int resource = CVRPConstants.RESOURCE_IS_DEMAND ? route.getDemand() : route.getDistance();
				min = Math.min(min, resource);
				max = Math.max(max, resource);
				budget += route.getDistance();
			}
			int upperBound = max - min + 1;
			CVRPSolution solution = new CVRPSolution(max - min, map);
			int alphaBudget = (int) Math.floor(alpha * budget);

			// Apply column generation.
			CVRPSolution newSolution = Solver.run(instance, budget, alphaBudget, upperBound,
					CVRPConstants.USE_U ? new int[K] : new int[K], timeLimit, rangeBranching);
			if (newSolution != null)
			{
				solution = newSolution;
			}

			// Check whether solutions are TSP.
			double currentRange = solution.getObjectiveValue();
			double trueMax = 0;
			double trueMin = Double.MAX_VALUE;
			for (Route route : solution.getRoutes())
			{
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
				trueMax = Math.max(trueMax, distance);
				trueMin = Math.min(trueMin, distance);
				System.out.println("Route: " + route.getDistance() + ". TSP: " + tsp.getObjectiveValue());
				tsp.cleanup();
			}
			double rangeTSP = trueMax - trueMin;
			System.out.println("True range: " + rangeTSP + " vs actual range: " + currentRange);

			sb.append(N + ";" + Writer.formatDouble(alpha) + ";" + t + ";" + Writer.formatDouble(currentRange) + ";"
					+ Writer.formatDouble(rangeTSP) + "\n");
		}

		// Write logger.
		Writer.write(Logger.getLogger().getOutput(), "logger_CVRP_" + setting + ".csv");
		Writer.write(sb.toString(), "tsp_" + setting + ".csv");
	}
}
