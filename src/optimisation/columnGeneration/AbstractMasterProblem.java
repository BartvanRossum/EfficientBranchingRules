package optimisation.columnGeneration;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ilog.concert.IloColumn;
import ilog.concert.IloConversion;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.BasisStatus;
import ilog.cplex.IloCplex.Status;
import optimisation.BAP.AbstractBranchingDecision;
import optimisation.columnGeneration.pricing.AbstractPricingProblem;
import util.Configuration;
import util.Pair;

public abstract class AbstractMasterProblem<T extends AbstractInstance, U extends AbstractColumn<T, V>, V extends AbstractPricingProblem<T>>
{
	protected final T instance;

	protected IloCplex cplex;
	protected IloObjective objective;

	protected Map<AbstractConstraint<T, U, V>, IloRange> constraintMap;
	protected List<AbstractConstraint<T, U, V>> branchingConstraints;
	protected List<AbstractBranchingDecision<T, U, V>> branchingDecisions;
	protected Map<AbstractConstraint<T, U, V>, IloNumVar> slackVarMap;

	// Dual smoothing parameters.
	protected DualVariables<T, U, V> currentDuals;
	protected DualVariables<T, U, V> smoothedDuals;
	protected int k = 1;
	protected final double alpha = Configuration.getConfiguration().getDoubleProperty("DUAL_SMOOTHING_ALPHA");
	protected double beta = 0;

	// Restricted master heuristic information.
	private double lowerBoundMILP;

	public AbstractMasterProblem(T instance) throws IloException
	{
		this.instance = instance;

		this.cplex = new IloCplex();
		this.objective = cplex.addMinimize();

		this.constraintMap = new LinkedHashMap<>();
		this.branchingConstraints = new ArrayList<>();
		this.branchingDecisions = new ArrayList<>();
		this.slackVarMap = new LinkedHashMap<>();

		this.currentDuals = new DualVariables<>();
		this.smoothedDuals = new DualVariables<>();

		// Set random seed for replication purposes.
		cplex.setParam(IloCplex.Param.RandomSeed, 129012);

		// Set the MIP tolerance lower than the default.
		cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 1e-06);

		// Turn off console output.
		cplex.setOut(null);
		cplex.setWarning(null);
	}

	public void updateDuals() throws IloException
	{
		// Set duals to initial values.
		for (AbstractConstraint<T, U, V> constraint : constraintMap.keySet())
		{
			double dual = cplex.getDual(constraintMap.get(constraint));
			currentDuals.set(constraint, dual);
		}

		// Update dual parameter.
		k = 1;
	}

	public void smootheDuals() throws IloException
	{
		// Update alpha and k.
		beta = Math.max(0, 1.0 - k * (1.0 - alpha));
		k++;

		// Compute smoothed duals.
		for (AbstractConstraint<T, U, V> constraint : constraintMap.keySet())
		{
			if (!smoothedDuals.contains(constraint))
			{
				smoothedDuals.set(constraint, currentDuals.get(constraint));
			}
			smoothedDuals.set(constraint,
					beta * smoothedDuals.get(constraint) + (1.0 - beta) * currentDuals.get(constraint));
		}
	}

	public double getBeta()
	{
		return beta;
	}
	
	public void setDuals(DualVariables<T, U, V> duals)
	{
		for (AbstractConstraint<T, U, V> constraint : duals.getConstraints())
		{
			double value = duals.get(constraint);
			currentDuals.set(constraint, value);
			smoothedDuals.set(constraint, value);
		}
	}
	
	public DualVariables<T, U, V> getCurrentDuals()
	{
		return currentDuals.getCopy();
	}
	
	public double getReducedCost(U column)
	{
		double reducedCost = column.getCoefficient();
		for (AbstractConstraint<T, U, V> constraint : constraintMap.keySet())
		{
			if (constraint.containsColumn(column))
			{
				double dual = currentDuals.get(constraint);
				double coefficient = constraint.getCoefficient(column);
				reducedCost -= dual * coefficient;
			}
		}
		return reducedCost;
	}

	public List<AbstractConstraint<T, U, V>> getConstraints()
	{
		return new ArrayList<>(constraintMap.keySet());
	}

	public void setOut(OutputStream outputStream) throws IloException
	{
		cplex.setOut(outputStream);
		cplex.setWarning(outputStream);
	}

	public void setTimeLimit(int seconds) throws IloException
	{
		cplex.setParam(IloCplex.Param.TimeLimit, seconds);
	}

	public double getObjectiveValue() throws IloException
	{
		return cplex.getObjValue();
	}

	public double getLowerBoundMILP() throws IloException
	{
		return lowerBoundMILP;
	}

	public double getDual(AbstractConstraint<T, U, V> constraint) throws IloException
	{
		return smoothedDuals.get(constraint);
	}

	public void setRightHandSide(AbstractConstraint<T, U, V> constraint, double coefficient) throws IloException
	{
		constraintMap.get(constraint).setUB(coefficient);
	}

	public void addConstraint(AbstractConstraint<T, U, V> constraint) throws IloException
	{
		IloRange range;
		switch (constraint.getModelConstraintType())
		{
			case EQUALITY:
				range = cplex.addEq(cplex.constant(0), constraint.getBound());
				break;
			case LESSER:
				range = cplex.addLe(cplex.constant(0), constraint.getBound());
				break;
			case GREATER:
				range = cplex.addGe(cplex.constant(0), constraint.getBound());
				break;
			default:
				range = null;
				break;
		}
		constraintMap.put(constraint, range);

		// Add a slack variable.
		constraint.addSlackVariable(this);
	}

	public void addConstraintWithExistingColumns(AbstractConstraint<T, U, V> constraint,
			List<Pair<U, IloNumVar>> columns)
			throws IloException
	{
		// Add all columns to left-hand side.
		IloNumExpr lhs = cplex.constant(0);
		for (Pair<U, IloNumVar> pair : columns)
		{
			if (constraint.containsColumn(pair.getKey()))
			{
				lhs = cplex.sum(lhs, cplex.prod(pair.getValue(), constraint.getCoefficient(pair.getKey())));
			}
		}

		IloRange range;
		switch (constraint.getModelConstraintType())
		{
			case EQUALITY:
				range = cplex.addEq(lhs, constraint.getBound());
				break;
			case LESSER:
				range = cplex.addLe(lhs, constraint.getBound());
				break;
			case GREATER:
				range = cplex.addGe(lhs, constraint.getBound());
				break;
			default:
				range = null;
				break;
		}
		if (constraintMap.containsKey(constraint))
		{
			System.out.println("Class: " + constraint.getClass());
			System.out.println("Type: " + constraint.getModelConstraintType());
			System.out.println("Bound: " + constraint.getBound());
			System.out.println(constraint);
			throw new IllegalArgumentException("ALREADY CONTAINED.");
		}
		constraintMap.put(constraint, range);

		// Add a slack variable.
		constraint.addSlackVariable(this);
	}

	public void addSlackVariable(AbstractConstraint<T, U, V> constraint, double coefficient, double cost,
			double lowerBound, double upperBound) throws IloException
	{
		IloColumn column = cplex.column(objective, cost);
		column = column.and(cplex.column(constraintMap.get(constraint), coefficient));
		slackVarMap.put(constraint, cplex.numVar(column, lowerBound, upperBound));
	}

	public double getSlackValue(AbstractConstraint<T, U, V> constraint) throws IloException
	{
		return cplex.getValue(slackVarMap.get(constraint));
	}

	public void removeSlackVariables() throws IloException
	{
		for (IloNumVar var : slackVarMap.values())
		{
			var.setLB(0);
			var.setUB(0);
		}
	}

	public abstract void fixColumn(U column, double value) throws IloException;

	public abstract void unfixColumn(U column) throws IloException;

	public Map<AbstractConstraint<T, U, V>, Double> getActiveSlackVariables() throws IloException
	{
		Map<AbstractConstraint<T, U, V>, Double> activeSlackMap = new LinkedHashMap<>();
		for (Entry<AbstractConstraint<T, U, V>, IloNumVar> entry : slackVarMap.entrySet())
		{
			if (cplex.getValue(entry.getValue()) > Configuration.getConfiguration().getDoubleProperty("PRECISION"))
			{
				activeSlackMap.put(entry.getKey(), cplex.getValue(entry.getValue()));
			}
		}
		return activeSlackMap;
	}

	public void export(String fileName) throws IloException
	{
		cplex.exportModel(fileName);
	}

	public void clean() throws IloException
	{
		cplex.clearModel();
		cplex.end();
	}

	public void solve() throws IloException
	{
		cplex.solve();
	}

	public boolean isFeasible() throws IloException
	{
		return cplex.isPrimalFeasible() && getActiveSlackVariables().size() == 0;
	}

	public Status getStatus() throws IloException
	{
		return cplex.getStatus();
	}

	public T getInstance()
	{
		return instance;
	}

	public void removeConstraint(AbstractConstraint<T, U, V> constraint) throws IloException
	{
		cplex.delete(constraintMap.get(constraint));
		constraintMap.remove(constraint);
	}

	public void undoBranchingDecision(AbstractBranchingDecision<T, U, V> branchingDecision) throws IloException
	{
		for (AbstractConstraint<T, U, V> branchingConstraint : branchingDecision.getBranchingConstraints())
		{
			cplex.delete(constraintMap.get(branchingConstraint));
			constraintMap.remove(branchingConstraint);
			branchingConstraints.remove(branchingConstraint);
		}
		branchingDecisions.remove(branchingDecision);
	}

	public void processBranchingDecision(AbstractBranchingDecision<T, U, V> branchingDecision) throws IloException
	{
		for (AbstractConstraint<T, U, V> branchingConstraint : branchingDecision.getBranchingConstraints())
		{
			branchingConstraints.add(branchingConstraint);
			processBranchingConstraint(branchingConstraint);
		}
		branchingDecisions.add(branchingDecision);
	}

	public List<AbstractBranchingDecision<T, U, V>> getBranchingDecisions()
	{
		return branchingDecisions;
	}

	public double getReducedCost(IloNumVar var) throws IloException
	{
		return cplex.getReducedCost(var);
	}

	public double getValue(IloNumVar var) throws IloException
	{
		return cplex.getValue(var);
	}

	public boolean isBasicVariable(IloNumVar var) throws IloException
	{
		return cplex.getBasisStatus(var).equals(BasisStatus.Basic);
	}

	public void setLowerBound(IloNumVar var, double lowerBound) throws IloException
	{
		var.setLB(lowerBound);
	}

	public void setUpperBound(IloNumVar var, double upperBound) throws IloException
	{
		var.setUB(upperBound);
	}

	public void setObjectiveCoefficient(IloNumVar var, double coefficient) throws IloException
	{
		cplex.setLinearCoef(objective, coefficient, var);
	}

	public void setConstraintCoefficient(IloNumVar var, AbstractConstraint<T, U, V> constraint, double coefficient)
			throws IloException
	{
		cplex.setLinearCoef(constraintMap.get(constraint), coefficient, var);
	}

	public IloNumVar addContinuousVariable(double lowerBound, double upperBound) throws IloException
	{
		IloNumVar var = cplex.numVar(lowerBound, upperBound);
		return var;
	}

	public abstract List<Pair<U, IloNumVar>> getColumns();

	public abstract void addColumn(U column) throws IloException;
	
	public void updateGenericDuals() throws IloException
	{
		for (AbstractConstraint<T, U, V> constraint : constraintMap.keySet())
		{
			constraint.updateGenericDuals(instance, smoothedDuals.get(constraint));
		}
	}

	public void updatePricingProblemDuals(V pricingProblem) throws IloException
	{
		for (AbstractConstraint<T, U, V> constraint : constraintMap.keySet())
		{
			constraint.updatePricingProblemDuals(pricingProblem, smoothedDuals.get(constraint));
		}
	}

	public abstract void processBranchingConstraint(AbstractConstraint<T, U, V> branchingConstraint)
			throws IloException;

	public AbstractSolution<T, U, V> applyRestrictedMasterHeuristic(int timeLimitSeconds)
			throws IloException
	{
		// Convert to MILP.
		List<IloConversion> conversions = new ArrayList<>();
		for (Pair<U, IloNumVar> pair : getColumns())
		{
			IloConversion conversion = cplex.conversion(pair.getValue(), IloNumVarType.Int);
			cplex.add(conversion);
			conversions.add(conversion);
		}

		// Solve MILP and store optimality gap.
		setTimeLimit(timeLimitSeconds);
		solve();
		this.lowerBoundMILP = cplex.getBestObjValue();
		AbstractSolution<T, U, V> solution = getSolution();

		// Convert back to LP.
		for (IloConversion conversion : conversions)
		{
			cplex.remove(conversion);
		}
		setTimeLimit(Integer.MAX_VALUE);
		return solution;
	}

	public abstract AbstractSolution<T, U, V> getSolution() throws IloException;

	public abstract void updateInactiveColumns() throws IloException;

	public abstract int removeInactiveColumns() throws IloException;
}
