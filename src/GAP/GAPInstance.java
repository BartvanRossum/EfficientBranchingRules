package GAP;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import optimisation.columnGeneration.AbstractInstance;

public class GAPInstance extends AbstractInstance
{
	private final int numAgents;
	private final int numJobs;

	private final int[] capacities;
	private final int[][] costs;
	private final int[][] profits;

	public GAPInstance(int numAgents, int numJobs, int[] capacities, int[][] costs, int[][] profits)
	{
		this.numAgents = numAgents;
		this.numJobs = numJobs;

		this.capacities = capacities;
		this.costs = costs;
		this.profits = profits;
	}

	public int getNumAgents()
	{
		return numAgents;
	}

	public int getNumJobs()
	{
		return numJobs;
	}

	public int getCapacity(int agent)
	{
		return capacities[agent];
	}

	public int getCost(int agent, int item)
	{
		return costs[agent][item];
	}

	public int getProfit(int agent, int item)
	{
		return profits[agent][item];
	}

	public static GAPInstance readInstance(String file) throws IOException
	{
		// Initialise reader.
		BufferedReader reader = new BufferedReader(new FileReader(file));

		// Read in all numbers in the file.
		List<Integer> numbers = new ArrayList<>();
		String line = reader.readLine().trim();
		while (line != null)
		{
			for (String data : line.trim().split("\\s+"))
			{
				numbers.add(Integer.valueOf(data));
			}
			line = reader.readLine();
		}
		reader.close();

		// Retrieve instance size.
		int numAgents = numbers.get(0);
		int numJobs = numbers.get(1);

		// Retrieve costs, payOffs, and capacities.
		int position = 2;
		int[][] costs = new int[numAgents][numJobs];
		int[][] profits = new int[numAgents][numJobs];
		int[] capacities = new int[numAgents];

		for (int i = 0; i < numAgents; i++)
		{
			for (int j = 0; j < numJobs; j++)
			{
				profits[i][j] = numbers.get(position);
				position++;
			}
		}
		for (int i = 0; i < numAgents; i++)
		{
			for (int j = 0; j < numJobs; j++)
			{
				costs[i][j] = numbers.get(position);
				position++;
			}
		}
		for (int i = 0; i < numAgents; i++)
		{
			capacities[i] = numbers.get(position);
			position++;
		}

		// Return instance.
		return new GAPInstance(numAgents, numJobs, capacities, costs, profits);
	}
	
	public GAPInstance addAgents(int toAdd)
	{
		// New number of agents.
		int newNumAgents = numAgents + toAdd;
		
		// Retrieve costs, payOffs, and capacities.
		int[][] newCosts = new int[newNumAgents][numJobs];
		int[][] newProfits = new int[newNumAgents][numJobs];
		int[] newCapacities = new int[newNumAgents];

		for (int i = 0; i < newNumAgents; i++)
		{
			for (int j = 0; j < numJobs; j++)
			{
				newProfits[i][j] = profits[0][j];
				newCosts[i][j] = costs[0][j];
			}
			newCapacities[i] = capacities[0] ;
		}

		// Return instance.
		return new GAPInstance(newNumAgents, numJobs, newCapacities, newCosts, newProfits);
	}
}
