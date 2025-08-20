package GAP.columnGeneration.branching;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import GAP.GAPInstance;
import GAP.columnGeneration.AssignmentColumn;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import optimisation.BAP.AbstractBranchingRule;
import optimisation.BAP.BAPNode;
import optimisation.BAP.BranchingCandidate;
import util.Configuration;

public class BranchingRuleJobAgent extends AbstractBranchingRule<GAPInstance, GAPColumn, GAPPricingProblem>
{
	public BranchingRuleJobAgent(double priority)
	{
		super(priority);
	}

	@Override
	public List<BranchingCandidate<GAPInstance, GAPColumn, GAPPricingProblem>> getBranchingCandidates(
			BAPNode<GAPInstance, GAPColumn, GAPPricingProblem> parent)
	{
		// Construct mapping of jobs to agents.
		Map<Integer, Map<Integer, Double>> agentJobMap = new LinkedHashMap<>();
		for (Entry<GAPColumn, Double> entry : parent.getSolution().getColumnMap().entrySet())
		{
			if (!(entry.getKey() instanceof AssignmentColumn))
			{
				continue;
			}
			AssignmentColumn column = (AssignmentColumn) entry.getKey();
			int agent = column.getAgent();
			if (!agentJobMap.containsKey(agent))
			{
				agentJobMap.put(agent, new LinkedHashMap<>());
			}
			for (int job : column.getJobs())
			{
				if (!agentJobMap.get(agent).containsKey(job))
				{
					agentJobMap.get(agent).put(job, 0.0);
				}
				agentJobMap.get(agent).put(job, agentJobMap.get(agent).get(job) + entry.getValue());
			}
		}

		// Generate branching candidates.
		List<BranchingCandidate<GAPInstance, GAPColumn, GAPPricingProblem>> candidates = new ArrayList<>();
		double PRECISION = Configuration.getConfiguration().getDoubleProperty("PRECISION");
		for (int agent : agentJobMap.keySet())
		{
			for (int job : agentJobMap.get(agent).keySet())
			{
				double value = agentJobMap.get(agent).get(job);
				double fractionalValue = Math.abs(value - Math.rint(value));
				if (fractionalValue > PRECISION)
				{
					BranchingCandidate<GAPInstance, GAPColumn, GAPPricingProblem> candidate = new BranchingCandidate<>(
							fractionalValue);
					candidate.addBranchingDecision(new BranchingDecisionJobAgent(agent, job, true));
					candidate.addBranchingDecision(new BranchingDecisionJobAgent(agent, job, false));
					candidates.add(candidate);
				}
			}
		}
		return candidates;
	}
}
