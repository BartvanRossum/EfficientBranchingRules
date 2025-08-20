package GAP.columnGeneration.pricing;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import GAP.GAPInstance;
import GAP.columnGeneration.AssignmentColumn;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.GAPSettings;
import optimisation.columnGeneration.AbstractMasterProblem;
import optimisation.columnGeneration.pricing.AbstractPricingProblemSolver;
import util.Configuration;
import util.Pair;

public class GAPPPSolver extends AbstractPricingProblemSolver<GAPInstance, GAPColumn, GAPPricingProblem>
{
	public GAPPPSolver()
	{
		super("dynamicProgrammingSolver");
	}

	@Override
	public List<Pair<GAPColumn, Double>> generateColumns(
			AbstractMasterProblem<GAPInstance, GAPColumn, GAPPricingProblem> masterProblem,
			GAPPricingProblem pricingProblem, double reducedCostThreshold, boolean enumerateColumns)
	{
		// Initialise a list of columns.
		List<Pair<GAPColumn, Double>> columns = new ArrayList<>();

		// Solve a knapsack problem for each agent.
		GAPInstance instance = pricingProblem.getInstance();
		int agent = pricingProblem.getAgent();
		int numJobs = instance.getNumJobs();

		// Set inputs.
		double[] values = new double[numJobs];
		int[] weights = new int[numJobs];
		for (int j = 0; j < numJobs; j++)
		{
			// We incorporate the dual of range and profit constraint here.
			values[j] = -pricingProblem.getJobDual(j) - instance.getCost(agent, j) * pricingProblem.getCostDual()
					+ instance.getProfit(agent, j) * pricingProblem.getProfitDual();
			if (!GAPSettings.MINIMISE_FAIRNESS)
			{
				values[j] -= instance.getProfit(agent, j);
			}
			weights[j] = instance.getCost(agent, j);
		}
		int maximumCapacity = Math.min(pricingProblem.getCostUpperBound(), instance.getCapacity(agent));
		int minimumCapacity = Math.max(pricingProblem.getCostLowerBound(), 0);
		int capacity = maximumCapacity;

		// Artificially increase the weight of forbidden jobs such that they are never
		// feasible.
		for (int job : pricingProblem.getForbiddenJobs())
		{
			weights[job] = capacity + 1;
		}

		// Artificially increase the weight of forced jobs such that they are not
		// selected, and reduce the capacity. The minimum capacity should be reduced
		// accordingly.
		double reducedCost = -pricingProblem.getAgentDual();
		for (int job : pricingProblem.getForcedJobs())
		{
			capacity -= weights[job];
			minimumCapacity -= weights[job];
			weights[job] = capacity + 1;
			reducedCost += values[job];
		}

		// Assignment is infeasible if cost of forced items exceeds capacity.
		if (capacity < 0)
		{
			return columns;
		}

		// Initialise DP variables.
		double[][] table = new double[numJobs + 1][capacity + 1];
		for (int i = 0; i <= numJobs; i++)
		{
			for (int j = 0; j <= capacity; j++)
			{
				table[i][j] = Double.MAX_VALUE;
			}
			table[i][0] = 0;
		}

		// Initialise a priority queue to store the best K column candidates.
		Queue<Pair<Integer, Double>> candidates = new PriorityQueue<>(
				(Pair<Integer, Double> p1, Pair<Integer, Double> p2) -> p1.getValue().compareTo(p2.getValue()));

		// Apply DP algorithm.
		for (int i = 1; i <= numJobs; i++)
		{
			for (int j = 1; j <= capacity; j++)
			{
				int weight = weights[i - 1];
				double value = values[i - 1];
				if (weight > j)
				{
					table[i][j] = table[i - 1][j];
				}
				else
				{
					// Recall that we are minimising reduced cost.
					table[i][j] = Math.min(table[i - 1][j], table[i - 1][j - weight] + value);
				}
			}
		}

		// Check potential candidates.

		int forcedWeight = 0;
		for (int job : pricingProblem.getForcedJobs())
		{
			forcedWeight += pricingProblem.getInstance().getCost(agent, job);
		}
		for (int j = Math.max(0, minimumCapacity); j <= capacity; j++)
		{
			double potentialReducedCost = reducedCost + table[numJobs][j]
					- pricingProblem.getValidDual(j + forcedWeight);
			if (potentialReducedCost < reducedCostThreshold)
			{
				candidates.add(new Pair<>(j, potentialReducedCost));
			}
		}

		// Add at most K reduced cost columns.
		for (int i = 0; i < Math.min(candidates.size(), Configuration.getConfiguration().getIntProperty("K")); i++)
		{
			// Retrieve index and reduced cost.
			Pair<Integer, Double> candidate = candidates.poll();

			// Retrieve optimal set of jobs, and add forced jobs.
			Set<Integer> jobs = knapsack(numJobs, candidate.getKey(), table, weights);
			for (int job : pricingProblem.getForcedJobs())
			{
				jobs.add(job);
			}

			// Compute parameters of assignment.
			int profit = 0;
			int cost = 0;
			for (int job : jobs)
			{
				// We frame profit maximisation as profit minimisation.
				profit -= instance.getProfit(agent, job);
				cost += instance.getCost(agent, job);
			}
			AssignmentColumn column = new AssignmentColumn(agent, profit, cost, jobs);
			columns.add(new Pair<>(column, candidate.getValue()));
		}

		// Return columns.
		return columns;
	}

	private Set<Integer> knapsack(int i, int j, double[][] table, int[] weights)
	{
		// Retrieve items in optimal knapack.
		if (i == 0)
		{
			return new LinkedHashSet<>();
		}
		if (table[i][j] < table[i - 1][j])
		{
			Set<Integer> jobs = knapsack(i - 1, j - weights[i - 1], table, weights);
			jobs.add(i - 1);
			return jobs;
		}
		else
		{
			return knapsack(i - 1, j, table, weights);
		}
	}
}
