
package GAP.models;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import GAP.GAPInstance;
import GAP.columnGeneration.AssignmentColumn;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

public class GAPMILP
{
	private final GAPInstance instance;
	private final int numAgents;
	private final int numJobs;

	private IloCplex cplex;

	private IloNumVar[][] assignmentVariables;

	public GAPMILP(GAPInstance instance) throws IloException
	{
		// Initialisation.
		this.instance = instance;
		this.numAgents = instance.getNumAgents();
		this.numJobs = instance.getNumJobs();

		this.cplex = new IloCplex();
		cplex.setOut(null);

		this.assignmentVariables = new IloNumVar[numAgents][numJobs];

		// Add variables.
		addAssignmentVariables();

		// Add constraints.
		addCapacityConstraints(instance);
		addAssignmentConstraints();

		// Add objective.
		addObjective(instance);
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

	private void addAssignmentVariables() throws IloException
	{
		for (int i = 0; i < numAgents; i++)
		{
			for (int j = 0; j < numJobs; j++)
			{
				assignmentVariables[i][j] = cplex.boolVar();
			}
		}
	}

	private void addAssignmentConstraints() throws IloException
	{
		for (int j = 0; j < numJobs; j++)
		{
			IloNumExpr lhs = cplex.constant(0);
			for (int i = 0; i < numAgents; i++)
			{
				lhs = cplex.sum(lhs, assignmentVariables[i][j]);
			}
			cplex.addEq(lhs, 1);
		}
	}

	private void addCapacityConstraints(GAPInstance instance) throws IloException
	{
		for (int i = 0; i < numAgents; i++)
		{
			IloNumExpr lhs = cplex.constant(0);
			for (int j = 0; j < numJobs; j++)
			{
				lhs = cplex.sum(lhs, cplex.prod(instance.getCost(i, j), assignmentVariables[i][j]));
			}
			cplex.addLe(lhs, instance.getCapacity(i));
		}
	}

	private void addObjective(GAPInstance instance) throws IloException
	{
		IloNumExpr obj = cplex.constant(0);
		for (int i = 0; i < numAgents; i++)
		{
			for (int j = 0; j < numJobs; j++)
			{
				obj = cplex.sum(obj, cplex.prod(instance.getProfit(i, j), assignmentVariables[i][j]));
			}
		}
		cplex.addMaximize(obj);
	}

	public List<AssignmentColumn> getSolution() throws UnknownObjectException, IloException
	{
		List<AssignmentColumn> solution = new ArrayList<>();
		for (int i = 0; i < numAgents; i++)
		{
			Set<Integer> jobs = new LinkedHashSet<>();
			int profit = 0;
			int cost = 0;
			for (int j = 0; j < numJobs; j++)
			{
				if (cplex.getValue(assignmentVariables[i][j]) > 0.5)
				{
					jobs.add(i);
					profit += instance.getProfit(i, j);
					cost += instance.getCost(i, j);
				}
			}
			solution.add(new AssignmentColumn(i, profit, cost, jobs));
		}
		return solution;
	}
}