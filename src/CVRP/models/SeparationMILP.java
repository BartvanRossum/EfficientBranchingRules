package CVRP.models;

import java.util.LinkedHashSet;
import java.util.Set;

import CVRP.instance.CVRPInstance;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class SeparationMILP
{
	private final CVRPInstance instance;
	private final int N;
	private final double[][] arcValues;

	private IloCplex cplex;

	private IloNumVar[] nodeVariables;
	private IloNumVar[][] Z;
	private IloNumVar Y;

	public SeparationMILP(CVRPInstance instance, double[][] arcValues) throws IloException
	{
		// Initialisation.
		this.instance = instance;
		this.N = instance.getN();
		this.arcValues = arcValues;

		this.cplex = new IloCplex();
		cplex.setOut(null);

		this.nodeVariables = new IloNumVar[N + 1];
		this.Z = new IloNumVar[N + 1][N + 1];
		this.Y = cplex.intVar(0, instance.getK());

		// Add variables.
		addNodeVariables();
		addZVariables();

		// Add constraints.
		addZConstraints();
		addYConstraints();

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

	public double getObjectiveValue() throws IloException
	{
		return cplex.getObjValue();
	}

	private void addNodeVariables() throws IloException
	{
		for (int i = 0; i <= N; i++)
		{
			nodeVariables[i] = cplex.boolVar();
		}
		nodeVariables[0].setUB(0);
	}

	private void addZConstraints() throws IloException
	{
		for (int i = 0; i <= N; i++)
		{
			for (int j = 0; j <= N; j++)
			{
				IloNumExpr rhs = cplex.constant(0);
				rhs = cplex.sum(rhs, nodeVariables[i]);
				rhs = cplex.sum(rhs, cplex.prod(-1, nodeVariables[j]));
				cplex.addGe(Z[i][j], rhs);
			}
		}
	}

	private void addZVariables() throws IloException
	{
		for (int i = 0; i <= N; i++)
		{
			for (int j = 0; j <= N; j++)
			{
				Z[i][j] = cplex.boolVar();
			}
		}
	}

	private void addYConstraints() throws IloException
	{
		IloNumExpr rhs = cplex.constant(0);
		for (int i = 0; i < N; i++)
		{
			double fraction = (double) instance.getDemands()[i] / instance.getQ();
			rhs = cplex.sum(rhs, cplex.prod(fraction, nodeVariables[i + 1]));
		}
		cplex.addGe(Y, rhs);

		IloNumExpr lhs = cplex.constant(0);
		lhs = cplex.sum(lhs, Y);
		double epsilon = 1.0 / instance.getQ();
		lhs = cplex.sum(lhs, epsilon - 1.0);
		cplex.addLe(lhs, rhs);
	}

	private void addObjective() throws IloException
	{
		IloNumExpr obj = cplex.constant(0);
		
		// Add z terms.
		for (int i = 0; i <= N; i++)
		{
			for (int j = 0; j <= N; j++)
			{
				obj = cplex.sum(obj, cplex.prod(arcValues[i][j], Z[i][j]));
			}
		}

		// Add y term.
		obj = cplex.sum(obj, cplex.prod(-1, Y));

		// Add to model.
		cplex.addMinimize(obj);
	}

	public void setObjectiveCoefficients(double[][] arcValues) throws IloException
	{
		int number = (N + 1) * (N + 1);
		double[] values = new double[number];
		IloNumVar[] variables = new IloNumVar[number];
		int index = 0;
		for (int i = 0; i <= N; i++)
		{
			for (int j = 0; j <= N; j++)
			{
				values[index] = arcValues[i][j];
				variables[index] = Z[i][j];
				index++;
			}
		}
		cplex.setLinearCoefs(cplex.getObjective(), values, variables);
	}

	public Set<Integer> getSolution() throws IloException
	{
		Set<Integer> solution = new LinkedHashSet<>();
		for (int i = 0; i <= N; i++)
		{
			if (cplex.getValue(nodeVariables[i]) > 0.5)
			{
				solution.add(i);
			}
		}
		return solution;
	}
}
