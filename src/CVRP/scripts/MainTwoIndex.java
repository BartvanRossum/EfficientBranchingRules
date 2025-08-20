package CVRP.scripts;

import java.io.IOException;
import java.util.List;

import CVRP.data.InstanceReader;
import CVRP.instance.CVRPInstance;
import CVRP.instance.Route;
import CVRP.models.TwoIndexMILP;
import CVRP.models.TwoIndexRoundedCapacityCuts;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import util.Writer;

public class MainTwoIndex
{
	public static void main(String[] args) throws IloException, IOException
	{
		// Parameter settings.
		int N = 25;
		int K = 5;
		for (int i = 0; i < 20; i++)
		{
			String file = "dataCVRP/n" + N + "_k" + K + "_" + i + ".txt";

			// Read instance.
			CVRPInstance instance = InstanceReader.readCVRPInstance(K, file);

			// Initialise model and solve.
			TwoIndexMILP model = new TwoIndexMILP(instance);

			// Add callbacks.
			long contextMask = IloCplex.Callback.Context.Id.Relaxation;
			model.addCallback(new TwoIndexRoundedCapacityCuts(instance, model.getArcVariables()), contextMask);

			// Solve.
			model.solve();
			System.out.println("Objective value: " + model.getObjectiveValue());
			List<Route> routes = model.getRoutes();
			System.out.println("#Routes: " + routes.size());
			model.clean();

			// Export solution.
			Writer.write(routes, "dataCVRP/solution_n" + N + "_k" + K + "_" + i + ".txt");
		}
	}
}
