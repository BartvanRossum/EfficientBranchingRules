package CVRP.scripts;

import java.io.IOException;
import java.util.List;

import CVRP.columnGeneration.Solver;
import CVRP.data.InstanceReader;
import CVRP.instance.CVRPConstants;
import CVRP.instance.CVRPInstance;
import CVRP.instance.Route;
import ilog.concert.IloException;
import util.Configuration;
import util.Logger;
import util.Util;
import util.Writer;

public class MainRangeBranching
{
	public static void main(String[] args) throws IloException, IOException
	{
		// Parameter settings.
		int[] numCustomers =
		{ 15, 20, 25 };
		int K = 5;
		double alpha = 1.1;
		int T = 20;
		boolean demandResource = false;
		boolean[] rangeBranchings =
		{ false, true };
		String[] formulations =
		{ "VEHICLE_INDEX", "LAST_CUSTOMER" };
		long timeLimit = 1 * 60 * 60 * 1000;

		int setting = Integer.valueOf(args[0]);
		int[] sizes =
		{ 3, 2, 2 };
		int[] indices = Util.retrieveSettings(sizes, setting);
		int N = numCustomers[indices[0]];
		String formulation = formulations[indices[1]];
		boolean rangeBranching = rangeBranchings[indices[2]];
		System.out.println(N + " " + formulation + " " + rangeBranching);

		// Choose the formulation.
		CVRPConstants.FORMULATION = formulation;
		CVRPConstants.RESOURCE_IS_DEMAND = demandResource;

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
			List<Route> routes = Route.readRoutes("dataCVRP/solution_n" + N + "_k" + K + "_" + t + ".txt", instance);
			int budget = 0;
			for (Route route : routes)
			{
				int resource = CVRPConstants.RESOURCE_IS_DEMAND ? route.getDemand() : route.getDistance();
				min = Math.min(min, resource);
				max = Math.max(max, resource);
				budget += route.getDistance();
			}
			int upperBound = max - min + 1;
			int alphaBudget = (int) Math.floor(alpha * budget);

			// Apply column generation.
			Solver.run(instance, budget, alphaBudget, upperBound, CVRPConstants.USE_U ? new int[K] : new int[K],
					timeLimit, rangeBranching);
		}

		// Write logger.
		Writer.write(Logger.getLogger().getOutput(), "logger_CVRP_" + setting + ".csv");
	}
}
