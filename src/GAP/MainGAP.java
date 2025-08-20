package GAP;

import java.io.IOException;
import java.util.List;

import GAP.columnGeneration.AllColumnSelector;
import GAP.columnGeneration.AssignmentColumn;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.GAPMasterProblem;
import GAP.columnGeneration.GAPSettings;
import GAP.columnGeneration.OrderColumn;
import GAP.columnGeneration.branching.BranchingRuleJobAgent;
import GAP.columnGeneration.branching.BranchingRuleRange;
import GAP.columnGeneration.constraints.AgentAssignmentConstraint;
import GAP.columnGeneration.constraints.AgentMaxConstraint;
import GAP.columnGeneration.constraints.AgentMinConstraint;
import GAP.columnGeneration.constraints.JobAssignmentConstraint;
import GAP.columnGeneration.constraints.MinimumProfitConstraint;
import GAP.columnGeneration.pricing.GAPPPSolver;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import GAP.columnGeneration.pricing.GAPPricingRoutine;
import GAP.models.GAPMILP;
import GAP.models.RangeGAPMILP;
import ilog.concert.IloException;
import optimisation.BAP.BranchAndPrice;
import optimisation.BAP.NodeComparators.BoundComparator;
import optimisation.columnGeneration.ColumnGeneration;
import util.Configuration;
import util.Util;
import util.Writer;

public class MainGAP
{
	public static void main(String[] args) throws IOException, IloException
	{
		// Read in instance.
		int index = Integer.valueOf(args[0]);
		String[] instances =
		{ "a", "b", "c" };
		String[] agents =
		{ "05", "10", "20" };
		String[] jobs =
		{ "100" };
		String[] methods =
		{ "MILP", "B&P", "RangeB&P" };
		double[] alphas =
		{ 0.9975, 0.995, 0.9925, 0.99 };
		int[] sizes =
		{ 3, 3, 1, 3, 4 };
		
		int[] settings = Util.retrieveSettings(sizes, index);	
		String instanceType = instances[settings[0]];
		String agent = agents[settings[1]];
		String job = jobs[settings[2]];
		String file = "dataGAP/" + instanceType + agent + job;
		String method = methods[settings[3]];
		double alpha = alphas[settings[4]];
		
		System.out.println(method + " " + job + " " + alpha);
		
		GAPInstance instance = GAPInstance.readInstance(file);
		int timeLimitSeconds = 60 * 60;

		// Initialise configuration file.
		String settingsFile = "defaultGAP.properties";
		Configuration.initialiseConfiguration(settingsFile, settingsFile);

		// Solve regular model.
		GAPMILP model = new GAPMILP(instance);
		model.solve();
		double profit = model.getObjectiveValue();
		List<AssignmentColumn> efficientSolution = model.getSolution();
		model.clean();
		System.out.println("Profit: " + profit);
		int max = 0;
		int min = Integer.MAX_VALUE;
		for (AssignmentColumn column : efficientSolution)
		{
			max = Math.max(max, column.getCost());
			min = Math.min(min, column.getCost());
		}
		int range = max - min;

		// Store output.
		StringBuilder sb = new StringBuilder();
		sb.append("instance;agents;jobs;alpha;method;LB;UB;time\n");
		sb.append(instanceType + ";" + agent + ";" + job + ";" + alpha + ";" + method + ";");
		System.out.println(sb.toString());
		double lowerBound = 0;
		double upperBound = 1000;
		long time = System.currentTimeMillis();

		if (method.equals("MILP"))
		{
			// Solve MILP.
			RangeGAPMILP rangeModel = new RangeGAPMILP(instance, alpha * profit);
			rangeModel.setTimeLimit(timeLimitSeconds);
			rangeModel.solve();
			upperBound = rangeModel.getObjectiveValue();
			lowerBound = rangeModel.getLowerBound();
			rangeModel.clean();
		}
		else
		{
			// Initialise master.
			GAPMasterProblem masterProblem = new GAPMasterProblem(instance);
			masterProblem.setOut(null);

			// Add agent assignment constraints.
			for (int i = 0; i < instance.getNumAgents(); i++)
			{
				masterProblem.addConstraint(new AgentAssignmentConstraint(i));
			}

			// Add job assignment constraints.
			for (int j = 0; j < instance.getNumJobs(); j++)
			{
				masterProblem.addConstraint(new JobAssignmentConstraint(j));
			}
			GAPSettings.MINIMISE_FAIRNESS = true;

			// Add minimum profit constraint.
			masterProblem.addConstraint(new MinimumProfitConstraint(-1 * alpha * profit));

			// Add range constraints.
			for (int i = 0; i < instance.getNumAgents(); i++)
			{
				masterProblem.addConstraint(new AgentMaxConstraint(i));
				masterProblem.addConstraint(new AgentMinConstraint(i));
			}

			// Add range variables.
			masterProblem.addColumn(new OrderColumn(0, instance.getNumAgents()));
			masterProblem.addColumn(new OrderColumn(instance.getNumAgents() - 1, instance.getNumAgents()));

			// Column generation settings.
			GAPPricingRoutine pricingRoutine = new GAPPricingRoutine();
			GAPPPSolver pricingSolver = new GAPPPSolver();
			AllColumnSelector selector = new AllColumnSelector();
			ColumnGeneration<GAPInstance, GAPColumn, GAPPricingProblem> columnGeneration = new ColumnGeneration<GAPInstance, GAPColumn, GAPPricingProblem>(
					pricingRoutine, pricingSolver, pricingSolver, selector);

			// Branch-and-price settings.
			BranchAndPrice<GAPInstance, GAPColumn, GAPPricingProblem> branchAndPrice = new BranchAndPrice<GAPInstance, GAPColumn, GAPPricingProblem>(
					new BoundComparator<GAPInstance, GAPColumn, GAPPricingProblem>(),
					instance, masterProblem, columnGeneration);

			branchAndPrice.addBranchingRule(new BranchingRuleJobAgent(1));
			if (method.equals("RangeB&P"))
			{
				branchAndPrice.addBranchingRule(new BranchingRuleRange(0));
			}

			// Add upper bound.
			branchAndPrice.setUpperBound(range);

			// Impose time limit.
			branchAndPrice.setTimeLimit(timeLimitSeconds * 1000);

			// Solve with branch-and-price.
			branchAndPrice.applyBranchAndPrice();

			// Update bounds.
			lowerBound = branchAndPrice.getLowerBound();
			upperBound = branchAndPrice.getUpperBound();
		}

		// Write output.
		time = System.currentTimeMillis() - time;
		sb.append(Writer.formatDouble(lowerBound) + ";" + Writer.formatDouble(upperBound) + ";" + time);
		Writer.write(sb.toString(), "run_" + index + ".csv");
	}
}
