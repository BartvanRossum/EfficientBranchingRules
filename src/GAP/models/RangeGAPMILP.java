
package GAP.models;

import GAP.GAPInstance;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class RangeGAPMILP
{
	private final int numAgents;
	private final int numJobs;

	private IloCplex cplex;

	private IloNumVar[][] assignmentVariables;
	private IloNumVar maximum;
	private IloNumVar minimum;

	public RangeGAPMILP(GAPInstance instance, double minimumPayOff) throws IloException
	{
		// Initialisation.
		this.numAgents = instance.getNumAgents();
		this.numJobs = instance.getNumJobs();

		this.cplex = new IloCplex();
		cplex.setOut(null);

		this.assignmentVariables = new IloNumVar[numAgents][numJobs];
		this.maximum = cplex.numVar(0, Double.MAX_VALUE);
		this.minimum = cplex.numVar(0, Double.MAX_VALUE);
		
		// Add variables.
		addAssignmentVariables();

		// Add constraints.
		addCapacityConstraints(instance);
		addAssignmentConstraints();
		addMinimumPayOffConstraint(instance, minimumPayOff);
		addRangeConstraints(instance);

		// Add objective.
		addObjective(instance);
	}
	
	public void export(String name) throws IloException
	{
		cplex.exportModel(name);
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
	
	public void setTimeLimit(int seconds) throws IloException
	{
		cplex.setParam(IloCplex.Param.TimeLimit, seconds);
	}
	
	public double getLowerBound() throws IloException
	{
		return cplex.getBestObjValue();
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
	
	private void addMinimumPayOffConstraint(GAPInstance instance, double minimumPayOff) throws IloException
	{
		IloNumExpr payOff = cplex.constant(0);
		for (int i = 0; i < numAgents; i++)
		{
			for (int j = 0; j < numJobs; j++)
			{
				payOff = cplex.sum(payOff, cplex.prod(instance.getProfit(i, j), assignmentVariables[i][j]));
			}
		}
		cplex.addGe(payOff, minimumPayOff);
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
	
	private void addRangeConstraints(GAPInstance instance) throws IloException
	{
		for (int i = 0; i < numAgents; i++)
		{
			IloNumExpr lhs = cplex.constant(0);
			for (int j = 0; j < numJobs; j++)
			{
				lhs = cplex.sum(lhs, cplex.prod(instance.getCost(i, j), assignmentVariables[i][j]));
			}
			cplex.addGe(maximum, lhs);
			cplex.addLe(minimum, lhs);
		}
	}

	private void addObjective(GAPInstance instance) throws IloException
	{
		IloNumExpr obj = cplex.constant(0);
		obj = cplex.sum(obj, maximum);
		obj = cplex.sum(obj, cplex.prod(-1, minimum));
		cplex.addMinimize(obj);
	}
}