package CVRP.columnGeneration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import CVRP.columnGeneration.pricing.CVRPPricingProblem;
import CVRP.instance.CVRPInstance;
import ilog.concert.IloColumn;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;
import optimisation.columnGeneration.AbstractSolution;
import util.Configuration;
import util.Pair;

public class CVRPMasterProblem extends AbstractMasterProblem<CVRPInstance, CVRPColumn, CVRPPricingProblem>
{
	private final Map<RouteColumn, IloNumVar> routeVarMap;
	private final Map<OrderColumn, IloNumVar> orderVarMap;

	public CVRPMasterProblem(CVRPInstance instance) throws IloException
	{
		super(instance);

		this.orderVarMap = new LinkedHashMap<>();
		this.routeVarMap = new LinkedHashMap<>();
	}

	@Override
	public List<Pair<CVRPColumn, IloNumVar>> getColumns()
	{
		List<Pair<CVRPColumn, IloNumVar>> columns = new ArrayList<>();
		for (Entry<RouteColumn, IloNumVar> entry : routeVarMap.entrySet())
		{
			columns.add(new Pair<>(entry.getKey(), entry.getValue()));
		}
		for (Entry<OrderColumn, IloNumVar> entry : orderVarMap.entrySet())
		{
			columns.add(new Pair<>(entry.getKey(), entry.getValue()));
		}
		return columns;
	}

	@Override
	public void addColumn(CVRPColumn column) throws IloException
	{
		if (routeVarMap.containsKey(column))
		{
			return;
		}

		// Initialise a new column.
		IloColumn columnVar = cplex.column(objective, column.getCoefficient());

		// Loop over all constraints.
		for (AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem> constraint : constraintMap.keySet())
		{
			if (constraint.containsColumn(column))
			{
				columnVar = columnVar.and(
						cplex.column(constraintMap.get(constraint), constraint.getCoefficient(column)));
			}
		}
		if (column instanceof RouteColumn)
		{
			RouteColumn routeColumn = (RouteColumn) column;
			routeVarMap.put(routeColumn, cplex.numVar(columnVar, 0, Double.MAX_VALUE));
		}
		else
		{
			OrderColumn orderColumn = (OrderColumn) column;
			orderVarMap.put(orderColumn, cplex.numVar(columnVar, 0, Double.MAX_VALUE));
		}
	}
	
	@Override
	public void processBranchingConstraint(
			AbstractConstraint<CVRPInstance, CVRPColumn, CVRPPricingProblem> branchingConstraint) throws IloException
	{
		addConstraintWithExistingColumns(branchingConstraint, getColumns());
	}

	@Override
	public AbstractSolution<CVRPInstance, CVRPColumn, CVRPPricingProblem> getSolution() throws IloException
	{
		// Add route columns.
		Map<CVRPColumn, Double> map = new LinkedHashMap<>();
		for (Entry<RouteColumn, IloNumVar> entry : routeVarMap.entrySet())
		{
			double value = cplex.getValue(entry.getValue());
			if (value > 0)
			{
				map.put(entry.getKey(), value);
			}
		}

		// Add range columns.
		for (Entry<OrderColumn, IloNumVar> entry : orderVarMap.entrySet())
		{
			double value = cplex.getValue(entry.getValue());
			map.put(entry.getKey(), value);
		}
		return new CVRPSolution(getObjectiveValue(), map);
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
		for (Entry<RouteColumn, IloNumVar> entry : routeVarMap.entrySet())
		{
			// Update the number of iterations in which the variable is not used.
			if (cplex.getValue(entry.getValue()) > precision)
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
		for (RouteColumn column : routeVarMap.keySet())
		{
			count += (column.getNumIterUnused() >= removalThreshold) ? 1 : 0;
		}

		// Construct an array with columns to be removed.
		IloNumVar[] removeColumns = new IloNumVar[count];
		int i = 0;
		Iterator<Entry<RouteColumn, IloNumVar>> iter = routeVarMap.entrySet().iterator();
		while (iter.hasNext())
		{
			Entry<RouteColumn, IloNumVar> entry = iter.next();
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
	public void fixColumn(CVRPColumn column, double value) throws IloException
	{
		if (column instanceof RouteColumn)
		{
			RouteColumn routeColumn = (RouteColumn) column;
			routeVarMap.get(routeColumn).setLB(value);
			routeVarMap.get(routeColumn).setUB(value);
		}
	}

	@Override
	public void unfixColumn(CVRPColumn column) throws IloException
	{
		if (column instanceof RouteColumn)
		{
			RouteColumn routeColumn = (RouteColumn) column;
			routeVarMap.get(routeColumn).setLB(0);
			routeVarMap.get(routeColumn).setUB(Double.MAX_VALUE);
		}
	}
}
