package CVRP.models;

import java.util.ArrayList;
import java.util.List;

import CVRP.instance.CVRPInstance;
import CVRP.instance.Route;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Callback.Function;

public class TwoIndexMILP
{
	private final CVRPInstance instance;
	private final int N;
	private final int K;

	private IloCplex cplex;

	private IloNumVar[][] arcVariables;
	private IloNumVar[] loadVariables;

	public TwoIndexMILP(CVRPInstance instance) throws IloException
	{
		// Initialisation.
		this.instance = instance;
		this.N = instance.getN();
		this.K = instance.getK();

		this.cplex = new IloCplex();
		cplex.setOut(null);

		this.arcVariables = new IloNumVar[N + 1][N + 1];
		this.loadVariables = new IloNumVar[N];

		// Add variables.
		addArcVariables();
		addLoadVariables();

		// Add constraints.
		addFlowConstraints();
		addLoadConstraints();
		addNumberVehicleConstraints();

		// Add objective.
		addObjective();
	}

	public void solve() throws IloException
	{
		cplex.solve();
	}

	public void clean() throws IloException
	{
		cplex.clearModel();
		cplex.end();
	}

	public void addCallback(Function callback, long contextMask) throws IloException
	{
		cplex.use(callback, contextMask);
	}

	public IloNumVar[][] getArcVariables()
	{
		return arcVariables;
	}

	public double getObjectiveValue() throws IloException
	{
		return cplex.getObjValue();
	}

	private void addArcVariables() throws IloException
	{
		for (int i = 0; i <= N; i++)
		{
			for (int j = 0; j <= N; j++)
			{
				arcVariables[i][j] = cplex.boolVar();
				if (i == j)
				{
					arcVariables[i][j].setUB(0);
				}
			}
		}
	}

	private void addLoadVariables() throws IloException
	{
		for (int i = 0; i < N; i++)
		{
			loadVariables[i] = cplex.numVar(instance.getDemands()[i], instance.getQ());
		}
	}

	private void addFlowConstraints() throws IloException
	{
		for (int i = 1; i <= N; i++)
		{
			IloNumExpr lhs = cplex.constant(0);
			IloNumExpr rhs = cplex.constant(0);
			for (int j = 0; j <= N; j++)
			{
				lhs = cplex.sum(lhs, arcVariables[i][j]);
				rhs = cplex.sum(rhs, arcVariables[j][i]);
			}
			cplex.addEq(lhs, 1);
			cplex.addEq(rhs, 1);
		}
	}

	private void addNumberVehicleConstraints() throws IloException
	{
		IloNumExpr flowOut = cplex.constant(0);
		for (int i = 1; i <= N; i++)
		{
			flowOut = cplex.sum(flowOut, arcVariables[0][i]);
		}
		cplex.addEq(flowOut, K);
	}

	private void addLoadConstraints() throws IloException
	{
		for (int i = 1; i <= N; i++)
		{
			for (int j = 1; j <= N; j++)
			{
				IloNumExpr lhs = cplex.constant(0);
				lhs = cplex.sum(lhs, loadVariables[j - 1]);
				lhs = cplex.sum(lhs, cplex.prod(-1, loadVariables[i - 1]));

				IloNumExpr rhs = cplex.constant(instance.getDemands()[j - 1] - instance.getQ());
				rhs = cplex.sum(rhs, cplex.prod(instance.getQ(), arcVariables[i][j]));
				cplex.addGe(lhs, rhs);
			}
		}
	}

	private void addObjective() throws IloException
	{
		IloNumExpr obj = cplex.constant(0);
		for (int i = 0; i <= N; i++)
		{
			for (int j = 0; j <= N; j++)
			{
				obj = cplex.sum(obj, cplex.prod(instance.getDistances()[i][j], arcVariables[i][j]));
			}
		}
		cplex.addMinimize(obj);
	}

	public void addMinCostConstraint(double minCost) throws IloException
	{
		IloNumExpr lhs = cplex.constant(0);
		for (int i = 0; i <= N; i++)
		{
			for (int j = 0; j <= N; j++)
			{
				lhs = cplex.sum(lhs, cplex.prod(instance.getDistances()[i][j], arcVariables[i][j]));
			}
		}
		cplex.addGe(lhs, minCost);
	}

	public List<Route> getRoutes() throws IloException
	{
		List<Route> routes = new ArrayList<>();
		int start = 1;
		int vehicleIndex = 0;
		while (true)
		{
			int currentIndex = 0;
			boolean go = false;

			for (int i = start; i <= N; i++)
			{
				if (cplex.getValue(arcVariables[0][i]) > 0.5)
				{
					currentIndex = i;
					start = i + 1;
					go = true;
					break;
				}
			}

			if (!go)
			{
				break;
			}

			List<Integer> nodes = new ArrayList<>();
			nodes.add(0);
			nodes.add(currentIndex);
			while (true)
			{
				for (int j = 0; j <= N; j++)
				{
					if (cplex.getValue(arcVariables[currentIndex][j]) > 0.5)
					{
						nodes.add(j);
						currentIndex = j;
						break;
					}
				}

				if (currentIndex == 0)
				{
					break;
				}
			}
			routes.add(new Route(vehicleIndex, nodes, instance));
			vehicleIndex++;
		}
		return routes;
	}
}