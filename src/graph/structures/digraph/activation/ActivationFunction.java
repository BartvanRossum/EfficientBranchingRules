package graph.structures.digraph.activation;

public interface ActivationFunction
{
	public boolean isActiveNode(Object node);
	
	public boolean isActiveArc(Object arc);
}
