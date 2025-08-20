package optimisation.columnGeneration;

import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloException;
import optimisation.columnGeneration.columnManagement.AbstractColumnSelector;
import optimisation.columnGeneration.pricing.AbstractPricingProblem;
import optimisation.columnGeneration.pricing.AbstractPricingProblemSolver;
import optimisation.columnGeneration.pricing.AbstractPricingRoutine;
import util.Configuration;
import util.Logger;
import util.Logger.CountQuantity;
import util.Logger.TimeQuantity;
import util.Logger.ValueQuantity;
import util.Pair;

public class ColumnGeneration<T extends AbstractInstance, U extends AbstractColumn<T, V>, V extends AbstractPricingProblem<T>>
{
	private boolean debug = false;
	private boolean useLogger = true;
	private final double PRECISION = Configuration.getConfiguration().getDoubleProperty("PRECISION");

	private final AbstractPricingRoutine<T, U, V> pricingRoutine;
	private final AbstractPricingProblemSolver<T, U, V> heuristicPricingProblemSolver;
	private final AbstractPricingProblemSolver<T, U, V> exactPricingProblemSolver;
	private final AbstractColumnSelector<T, U, V> columnSelector;

	public ColumnGeneration(AbstractPricingRoutine<T, U, V> pricingRoutine,
			AbstractPricingProblemSolver<T, U, V> heuristicPricingProblemSolver,
			AbstractPricingProblemSolver<T, U, V> exactPricingProblemSolver,
			AbstractColumnSelector<T, U, V> columnSelector)
	{
		this.pricingRoutine = pricingRoutine;
		this.heuristicPricingProblemSolver = heuristicPricingProblemSolver;
		this.exactPricingProblemSolver = exactPricingProblemSolver;
		this.columnSelector = columnSelector;
	}

	public void enableLogger()
	{
		useLogger = true;
	}

	public void disableLogger()
	{
		useLogger = false;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	public AbstractPricingProblemSolver<T, U, V> getPricingProblemSolver(boolean exact)
	{
		if (exact)
		{
			return exactPricingProblemSolver;
		}
		else
		{
			return heuristicPricingProblemSolver;
		}
	}

	public AbstractPricingRoutine<T, U, V> getPricingRoutine()
	{
		return pricingRoutine;
	}

	public void applyColumnGeneration(AbstractMasterProblem<T, U, V> masterProblem, T instance, double lowerBound)
			throws IloException
	{
		// Set pricing settings and store past objective values.
		PricingSettings.EXACT_PRICING = PricingSettings.START_EXACT_PRICING;
		double previousObjective = Double.MAX_VALUE;
		Logger logger = Logger.getLogger();
		if (!useLogger)
		{
			logger = Logger.getDummyLogger();
		}

		// Generate the pricing problems once.
		pricingRoutine.constructPricingProblems(masterProblem, instance);

		boolean go = true;
		while (go)
		{
			go = false;

			// Solve LP relaxation.
			long timeRMP = System.currentTimeMillis();
			logger.startTimer(TimeQuantity.TIME_RMP);
			masterProblem.solve();
			logger.stopTimer(TimeQuantity.TIME_RMP);
			timeRMP = System.currentTimeMillis() - timeRMP;

			// Retrieve objective value.
			double objectiveValue = masterProblem.getObjectiveValue();
			logger.setValue(ValueQuantity.VALUE_OBJECTIVE, objectiveValue);

			// Update incumbent dual values.
			masterProblem.updateDuals();

			// Apply column management, if a column manager is selected.
			logger.startTimer(TimeQuantity.TIME_COL_MANAGEMENT);
			masterProblem.updateInactiveColumns();
			logger.stopTimer(TimeQuantity.TIME_COL_MANAGEMENT);

			// Dual smoothing framework.
			List<U> columns = new ArrayList<>();
			long timePricing = System.currentTimeMillis();
			while (true)
			{
				// Compute smoothed duals.
				masterProblem.smootheDuals();

				// Solve pricing problems.
				logger.incrementCount(CountQuantity.NUM_ITERATION_PRICING, 1);
				logger.startTimer(TimeQuantity.TIME_PRICING);
				AbstractPricingProblemSolver<T, U, V> pricingProblemSolver = PricingSettings.EXACT_PRICING
						? exactPricingProblemSolver
						: heuristicPricingProblemSolver;
				List<Pair<U, Double>> generatedColumns = pricingRoutine.generateColumns(masterProblem,
						pricingProblemSolver, instance);

				logger.stopTimer(TimeQuantity.TIME_PRICING);
				logger.incrementCount(CountQuantity.NUM_GENERATED_COL, generatedColumns.size());

				// Select a subset of columns, filtering on reduced cost.
				logger.startTimer(TimeQuantity.TIME_COL_MANAGEMENT);
				for (U column : columnSelector.selectColumns(generatedColumns))
				{
					double reducedCost = masterProblem.getReducedCost(column);
					if (reducedCost < -PRECISION)
					{
						columns.add(column);
					}
				}
				logger.stopTimer(TimeQuantity.TIME_COL_MANAGEMENT);
				logger.incrementCount(CountQuantity.NUM_SELECTED_COL, columns.size());

				// Break from pricing iteration if we identified negative reduced cost columns.
				if (columns.size() > 0 || masterProblem.getBeta() < PRECISION || !PricingSettings.EXACT_PRICING)
				{
					break;
				}
			}
			timePricing = System.currentTimeMillis() - timePricing;

			// Debugging purposes
			if (debug)
			{
				System.out.println("Obj: " + objectiveValue + "#cols: " + masterProblem.getColumns().size()
						+ ". Pricing: " + PricingSettings.EXACT_PRICING + " " + columns.size() + ". TimeRMP: " + timeRMP
						+ ". Time pricing: " + timePricing + ". Feasible: " + masterProblem.isFeasible());
			}

			// Stop if no columns are found.
			if (columns.size() == 0)
			{
				// Switch to exact pricing if necessary.
				if (!PricingSettings.EXACT_PRICING && PricingSettings.SWITCH_TO_EXACT_PRICING)
				{
					go = true;
					PricingSettings.EXACT_PRICING = true;
				}
			}
			else
			{
				// Determine whether to continue or not based on improvement criterion.
				double deltaObjective = Math.max(100.0 * (previousObjective - objectiveValue) / previousObjective, 0);
				if (Math.abs(objectiveValue - lowerBound) < PRECISION)
				{
					go = false;
				}
				else
				{
					// Perform column management. If we are in an improvement iteration, we remove
					// columns.
					logger.startTimer(TimeQuantity.TIME_COL_MANAGEMENT);
					if (deltaObjective > PRECISION)
					{
						masterProblem.removeInactiveColumns();
					}

					// Add columns.
					for (U column : columns)
					{
						masterProblem.addColumn(column);
					}
					go = true;
					logger.stopTimer(TimeQuantity.TIME_COL_MANAGEMENT);
				}
			}

			// Update stored objective value.
			previousObjective = objectiveValue;
		}
	}
}
