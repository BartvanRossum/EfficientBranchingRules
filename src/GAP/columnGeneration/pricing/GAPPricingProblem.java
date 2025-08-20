package GAP.columnGeneration.pricing;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import GAP.GAPInstance;
import optimisation.columnGeneration.pricing.AbstractPricingProblem;

public class GAPPricingProblem extends AbstractPricingProblem<GAPInstance>
{
	private final GAPInstance instance;
	private final int agent;

	private Map<Integer, Double> jobDuals;
	private double agentDual;
	private double costDual;
	private double profitDual;
	private double[] validDuals;

	private Set<Integer> forcedJobs;
	private Set<Integer> forbiddenJobs;

	private int costLowerBound = 0;
	private int costUpperBound = Integer.MAX_VALUE;

	public GAPPricingProblem(GAPInstance instance, int agent)
	{
		this.instance = instance;
		this.agent = agent;

		this.jobDuals = new LinkedHashMap<>();
		this.validDuals = new double[instance.getCapacity(agent) + 1];

		this.forcedJobs = new LinkedHashSet<>();
		this.forbiddenJobs = new LinkedHashSet<>();
	}

	public GAPInstance getInstance()
	{
		return instance;
	}

	public int getAgent()
	{
		return agent;
	}
	
	public void resetDuals()
	{
		agentDual = 0;
		costDual = 0;
		profitDual = 0;
		validDuals = new double[validDuals.length];
	}

	public void addValidDuals(int from, int to, double dual)
	{
		for (int i = from; i <= to; i++)
		{
			validDuals[i] += dual;
		}
	}
	
	public double getValidDual(int index)
	{
		return validDuals[index];
	}

	public void setJobDual(int job, double dual)
	{
		jobDuals.put(job, dual);
	}

	public double getJobDual(int job)
	{
		return jobDuals.get(job);
	}

	public void setAgentDual(double dual)
	{
		this.agentDual = dual;
	}

	public double getAgentDual()
	{
		return agentDual;
	}

	public void addCostDual(double dual)
	{
		this.costDual += dual;
	}

	public double getCostDual()
	{
		return costDual;
	}

	public void setProfitDual(double dual)
	{
		this.profitDual = dual;
	}

	public double getProfitDual()
	{
		return profitDual;
	}

	public void addForcedJob(int job)
	{
		forcedJobs.add(job);
	}

	public Set<Integer> getForcedJobs()
	{
		return forcedJobs;
	}

	public void addForbiddenJob(int job)
	{
		forbiddenJobs.add(job);
	}

	public Set<Integer> getForbiddenJobs()
	{
		return forbiddenJobs;
	}

	public void setCostLowerBound(int costLowerBound)
	{
		this.costLowerBound = Math.max(costLowerBound, this.costLowerBound);
	}

	public int getCostLowerBound()
	{
		return costLowerBound;
	}

	public void setCostUpperBound(int costUpperBound)
	{
		this.costUpperBound = Math.min(costUpperBound, this.costUpperBound);
	}

	public int getCostUpperBound()
	{
		return costUpperBound;
	}
}
