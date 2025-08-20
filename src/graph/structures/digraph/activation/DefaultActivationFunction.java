package graph.structures.digraph.activation;

public class DefaultActivationFunction implements ActivationFunction
{
	@Override
	public boolean isActiveNode(Object node)
	{
		return true;
	}

	@Override
	public boolean isActiveArc(Object arc)
	{
		return true;
	}
}
