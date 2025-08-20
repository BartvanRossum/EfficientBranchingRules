package CVRP.data;

import java.io.IOException;

import CVRP.instance.CVRPInstance;
import util.Writer;

public class GenerateInstances
{
	public static void main(String[] args) throws IOException
	{
		// Settings.
		int N = 15;
		int numInstances = 20;
		int K = 5;

		// Read in large instance.
		CVRPInstance instance = LibReader.readInstance(K, "dataCVRP/X_64.txt");
		System.out.println(instance.getK() + " " + instance.getN() + " " + instance.getQ());

		// Generate 20 instances of smaller size, the first customer being the depot.
		for (int k = 0; k < numInstances; k++)
		{
			// Set demands.
			int startIndex = k * (N + 1);
			int[] demands = new int[N];
			int totalDemand = 0;
			for (int i = 0; i < N; i++)
			{
				demands[i] = instance.getDemands()[startIndex + i + 1];
				totalDemand += demands[i];
			}

			// Set distances.
			int[][] distances = new int[N + 1][N + 1];
			for (int i = 0; i <= N; i++)
			{
				for (int j = 0; j <= N; j++)
				{
					distances[i][j] = instance.getDistances()[startIndex + i][startIndex + j];
				}
			}

			// Compute capacity of new instance.
			int Q = (int) Math.ceil(totalDemand / (K - 1));

			// Generate new instance.
			CVRPInstance newInstance = new CVRPInstance(N, K, Q, demands, distances);
			writeInstance(newInstance, "dataCVRP/n" + N + "_k" + K + "_" + k + ".txt");
		}
	}

	public static void writeInstance(CVRPInstance instance, String outputFile) throws IOException
	{
		StringBuilder sb = new StringBuilder();

		sb.append("Q:\n");
		sb.append(instance.getQ() + "\n");

		sb.append("n:\n");
		sb.append(instance.getN() + "\n");

		sb.append("Demands:\n");
		for (int i = 1; i <= instance.getN(); i++)
		{
			sb.append(i + "\t" + instance.getDemands()[i - 1] + "\n");
		}

		sb.append("Cost-matrix:\n");
		for (int j = 0; j <= instance.getN(); j++)
		{
			for (int i = 0; i <= instance.getN(); i++)
			{
				sb.append(instance.getDistances()[i][j] + "\t");
			}
			sb.append("\n");
		}
		Writer.write(sb.toString(), outputFile);
	}
}
