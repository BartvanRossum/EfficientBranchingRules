package GAP.columnGeneration;

import java.util.LinkedHashSet;
import java.util.Set;

public class AssignmentColumn extends GAPColumn
{
	private final int agent;
	private final int profit;
	private final int cost;

	private final Set<Integer> jobs;

	public AssignmentColumn(int agent, int profit, int cost, Set<Integer> jobs)
	{
		super(GAPSettings.MINIMISE_FAIRNESS ? 0 : profit);

		this.agent = agent;
		this.profit = profit;
		this.cost = cost;

		this.jobs = jobs;
	}

	public int getAgent()
	{
		return agent;
	}

	public int getCost()
	{
		return cost;
	}

	public int getProfit()
	{
		return profit;
	}

	public Set<Integer> getJobs()
	{
		return jobs;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + agent;
		result = prime * result + cost;
		result = prime * result + ((jobs == null) ? 0 : jobs.hashCode());
		result = prime * result + profit;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		AssignmentColumn other = (AssignmentColumn) obj;
		if (agent != other.agent) return false;
		if (cost != other.cost) return false;
		if (jobs == null)
		{
			if (other.jobs != null) return false;
		}
		else if (!jobs.equals(other.jobs)) return false;
		if (profit != other.profit) return false;
		return true;
	}
	
	public static AssignmentColumn readColumn(String column)
	{
		String[] data = column.split(" ");
		int agent = Integer.valueOf(data[0]);
		int profit = Integer.valueOf(data[1]);
		int cost = Integer.valueOf(data[2]);
		Set<Integer> jobs = new LinkedHashSet<>();
		for (int i = 3; i< data.length; i++)
		{
			jobs.add(Integer.valueOf(data[i]));
		}
		return new AssignmentColumn(agent, profit, cost, jobs);
	}

	@Override
	public String toString()
	{
		String result = agent + " " + profit + " " + cost;
		for (int job : jobs)
		{
			result += " " + job;
		}
		return result;
	}
}
