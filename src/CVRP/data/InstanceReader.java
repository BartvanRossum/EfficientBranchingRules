package CVRP.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import CVRP.instance.CVRPInstance;

public class InstanceReader
{
	public static CVRPInstance readCVRPInstance(int K, String file) throws IOException
	{
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

		// Read Q.
		String line = bufferedReader.readLine();
		line = bufferedReader.readLine();
		int Q = Integer.valueOf(line);

		// Read N.
		line = bufferedReader.readLine();
		line = bufferedReader.readLine();
		int N = Integer.valueOf(line);

		// Read demands.
		line = bufferedReader.readLine();
		int[] demands = new int[N];
		for (int i = 0; i < N; i++)
		{
			line = bufferedReader.readLine();
			String[] data = line.split("\\s+");
			demands[i] = Integer.valueOf(data[1]);
		}

		// Read distances.
		int[][] distances = new int[N + 1][N + 1];
		line = bufferedReader.readLine();
		for (int i = 0; i <= N; i++)
		{
			line = bufferedReader.readLine();
			String[] data = line.split("\\s+");
			for (int j = 0; j <= N; j++)
			{
				distances[i][j] = (int) Math.rint(Double.valueOf(data[j]));
			}
		}
		bufferedReader.close();

		return new CVRPInstance(N, K, Q, demands, distances);
	}
}
