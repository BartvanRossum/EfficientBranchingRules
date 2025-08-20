package CVRP.models;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class TSP
{
	private final double[][] distances;
	private final int n;

	private IloCplex cplex;

	private IloNumVar[][] pathVars;
	private IloNumVar[] dummyVars;

	public TSP(double[][] distances) throws IloException
	{
		// Initialize the instance variables
		this.distances = distances;
		this.n = distances.length;

		this.pathVars = new IloNumVar[n][n];
		this.dummyVars = new IloNumVar[n - 1];
		this.cplex = new IloCplex();

		// Initialize the model.
		addVariables();
		addIngoingConstraints();
		addOutgoingConstraints();
		addTourConstraints();
		addObjective();

		cplex.setOut(null);
		cplex.solve();
	}

	public void solve() throws IloException
	{
		cplex.solve();
	}

	/**
	 * Checks whether the current solution to the model is feasible
	 * 
	 * @return the feasibility of the model
	 * @throws IloException if something is wrong with CPLEX
	 */
	public boolean isFeasible() throws IloException
	{
		return cplex.isPrimalFeasible();
	}

	public void cleanup() throws IloException
	{
		cplex.clearModel();
		cplex.end();
	}

	private void addOutgoingConstraints() throws IloException
	{
		for (int i = 0; i < n; i++)
		{
			IloNumExpr expr = cplex.constant(0);
			for (int j = 0; j < n; j++)
			{
				if (i != j)
				{
					expr = cplex.sum(expr, pathVars[i][j]);
				}
			}
			cplex.addEq(1, expr);
		}
	}

	private void addIngoingConstraints() throws IloException
	{
		for (int i = 0; i < n; i++)
		{
			IloNumExpr expr = cplex.constant(0);
			for (int j = 0; j < n; j++)
			{
				if (i != j)
				{
					expr = cplex.sum(expr, pathVars[j][i]);
				}
			}
			cplex.addEq(1, expr);
		}
	}

	private void addTourConstraints() throws IloException
	{
		for (int i = 1; i < n; i++)
		{
			for (int j = 1; j < n; j++)
			{
				if (i != j)
				{
					IloNumExpr expr = cplex.constant(0);
					expr = cplex.sum(expr, dummyVars[i - 1]);
					expr = cplex.sum(expr, cplex.prod(-1, dummyVars[j - 1]));
					expr = cplex.sum(expr, cplex.prod(n, pathVars[i][j]));
					cplex.addLe(expr, n - 1);
				}
			}
		}
	}

	private void addObjective() throws IloException
	{
		// Initialize the objective sum to 0
		IloNumExpr obj = cplex.constant(0);
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < n; j++)
			{
				if (j != i)
				{
					obj = cplex.sum(obj, cplex.prod(distances[i][j], pathVars[i][j]));
				}
			}
		}

		// Add the obj expression as a minimization objective
		cplex.addMinimize(obj);
	}

	public double getObjectiveValue() throws IloException
	{
		return cplex.getObjValue();
	}

	public void printSolution() throws IloException
	{
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < n; j++)
			{
				if (i == j)
				{
					continue;
				}
				if (cplex.getValue(pathVars[i][j]) > 0.5)
				{
					System.out.println(i + " ->  " + j);
				}
			}
		}
	}

	private void addVariables() throws IloException
	{
		for (int i = 0; i < n; i++)
		{
			if (i > 0)
			{
				IloNumVar sequenceVar = cplex.numVar(0, n - 1);
				dummyVars[i - 1] = sequenceVar;
			}
			for (int j = 0; j < n; j++)
			{
				IloNumVar pathVar = cplex.boolVar();
				pathVars[i][j] = pathVar;
			}
		}
	}
}
