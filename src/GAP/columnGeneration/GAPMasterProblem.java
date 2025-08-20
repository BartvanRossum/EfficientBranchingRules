package GAP.columnGeneration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import GAP.GAPInstance;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import ilog.concert.IloColumn;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;
import optimisation.columnGeneration.AbstractSolution;
import util.Configuration;
import util.Pair;

public class GAPMasterProblem extends AbstractMasterProblem<GAPInstance, GAPColumn, GAPPricingProblem>
{
	private final Map<AssignmentColumn, IloNumVar> assignmentMap;
	private final Map<OrderColumn, IloNumVar> orderMap;

	public GAPMasterProblem(GAPInstance instance) throws IloException
	{
		super(instance);

		this.assignmentMap = new LinkedHashMap<>();
		this.orderMap = new LinkedHashMap<>();
	}

	@Override
	public List<Pair<GAPColumn, IloNumVar>> getColumns()
	{
		List<Pair<GAPColumn, IloNumVar>> columns = new ArrayList<>();
		for (Entry<AssignmentColumn, IloNumVar> entry : assignmentMap.entrySet())
		{
			columns.add(new Pair<>(entry.getKey(), entry.getValue()));
		}
		for (Entry<OrderColumn, IloNumVar> entry : orderMap.entrySet())
		{
			columns.add(new Pair<>(entry.getKey(), entry.getValue()));
		}
		return columns;
	}

	@Override
	public void addColumn(GAPColumn column) throws IloException
	{
		if (assignmentMap.containsKey(column))
		{
			return;
		}

		// Initialise a new column.
		IloColumn columnVar = cplex.column(objective, column.getCoefficient());

		// Loop over all constraints.
		for (AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem> constraint : constraintMap.keySet())
		{
			if (constraint.containsColumn(column))
			{
				columnVar = columnVar.and(
						cplex.column(constraintMap.get(constraint), constraint.getCoefficient(column)));
			}
		}
		if (column instanceof AssignmentColumn)
		{
			AssignmentColumn assignmentColumn = (AssignmentColumn) column;
			assignmentMap.put(assignmentColumn, cplex.numVar(columnVar, 0, Double.MAX_VALUE));
		}
		else
		{
			OrderColumn orderColumn = (OrderColumn) column;
			orderMap.put(orderColumn, cplex.numVar(columnVar, 0, Double.MAX_VALUE));
		}
	}

	@Override
	public void processBranchingConstraint(
			AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem> branchingConstraint) throws IloException
	{
		addConstraintWithExistingColumns(branchingConstraint, getColumns());
	}

	@Override
	public AbstractSolution<GAPInstance, GAPColumn, GAPPricingProblem> getSolution() throws IloException
	{
		double PRECISION = Configuration.getConfiguration().getDoubleProperty("PRECISION");
		Map<GAPColumn, Double> solution = new LinkedHashMap<>();
		for (Entry<AssignmentColumn, IloNumVar> entry : assignmentMap.entrySet())
		{
			double value = cplex.getValue(entry.getValue());
			if (value > PRECISION)
			{
				solution.put(entry.getKey(), value);
			}
		}
		for (Entry<OrderColumn, IloNumVar> entry : orderMap.entrySet())
		{
			double value = cplex.getValue(entry.getValue());
			if (value > PRECISION)
			{
				solution.put(entry.getKey(), value);
			}
		}
		return new GAPSolution(getObjectiveValue(), solution);
	}

	@Override
	public boolean isFeasible() throws IloException
	{
		double precision = Configuration.getConfiguration().getDoubleProperty("PRECISION");
		for (IloNumVar var : slackVarMap.values())
		{
			if (cplex.getValue(var) > precision)
			{
				return false;
			}
		}
		return cplex.isPrimalFeasible();
	}

	@Override
	public void updateInactiveColumns() throws IloException
	{
		double precision = Configuration.getConfiguration().getDoubleProperty(
				"PRECISION");
		double reducedCostThreshold = Configuration.getConfiguration().getDoubleProperty(
				"COLUMN_INACTIVE_REDUCED_COST_THRESHOLD");
		for (Entry<AssignmentColumn, IloNumVar> entry : assignmentMap.entrySet())
		{
			// Update the number of iterations in which the variable is not used.
			boolean used = cplex.getReducedCost(entry.getValue()) < reducedCostThreshold;
			if (!used)
			{
				used = cplex.getValue(entry.getValue()) > precision;
			}

			if (used)
			{
				entry.getKey().resetNumIterUnused();
			}
			else
			{
				entry.getKey().increaseNumIterUnused();
			}
		}
	}

	@Override
	public int removeInactiveColumns() throws IloException
	{
		// Retrieve column removal threshold.
		int removalThreshold = Configuration.getConfiguration()
											.getIntProperty("COLUMN_REMOVAL_THRESHOLD");

		// Count the number of columns to be removed.
		int count = 0;
		for (AssignmentColumn column : assignmentMap.keySet())
		{
			count += (column.getNumIterUnused() >= removalThreshold) ? 1 : 0;
		}

		// Construct an array with columns to be removed.
		IloNumVar[] removeColumns = new IloNumVar[count];
		int i = 0;
		Iterator<Entry<AssignmentColumn, IloNumVar>> iter = assignmentMap.entrySet().iterator();
		while (iter.hasNext())
		{
			Entry<AssignmentColumn, IloNumVar> entry = iter.next();
			if (entry.getKey().getNumIterUnused() >= removalThreshold)
			{
				removeColumns[i] = entry.getValue();
				iter.remove();
				i++;
			}
		}

		// Remove columns from the model.
		cplex.end(removeColumns);
		return count;
	}

	@Override
	public void fixColumn(GAPColumn column, double value) throws IloException
	{
		if (column instanceof AssignmentColumn)
		{
			AssignmentColumn assignmentColumn = (AssignmentColumn) column;
			assignmentMap.get(assignmentColumn).setLB(value);
			assignmentMap.get(assignmentColumn).setUB(value);
		}
	}

	@Override
	public void unfixColumn(GAPColumn column) throws IloException
	{
		if (column instanceof AssignmentColumn)
		{
			AssignmentColumn assignmentColumn = (AssignmentColumn) column;
			assignmentMap.get(assignmentColumn).setLB(0);
			assignmentMap.get(assignmentColumn).setUB(Double.MAX_VALUE);
		}
	}
}
