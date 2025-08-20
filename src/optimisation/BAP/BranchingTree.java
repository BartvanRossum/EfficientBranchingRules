package optimisation.BAP;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import optimisation.BAP.NodeComparators.DynamicComparator;
import optimisation.columnGeneration.AbstractColumn;
import optimisation.columnGeneration.AbstractInstance;
import optimisation.columnGeneration.pricing.AbstractPricingProblem;

public class BranchingTree<T extends AbstractInstance, U extends AbstractColumn<T, V>, V extends AbstractPricingProblem<T>>
{
	private PriorityQueue<BAPNode<T, U, V>> queue;
	private double upperBound = Double.MAX_VALUE;
	private double lowerBound = Double.MIN_VALUE;

	public BranchingTree(Comparator<BAPNode<T, U, V>> comparator)
	{
		this.queue = new PriorityQueue<>(comparator);
	}

	public int getNumberOfNodes()
	{
		return queue.size();
	}

	public boolean isEmpty()
	{
		return queue.isEmpty();
	}

	public void enqueue(BAPNode<T, U, V> node)
	{
		queue.add(node);
	}

	public BAPNode<T, U, V> dequeue()
	{
		return queue.remove();
	}

	public void updateLowerBound()
	{
		lowerBound = upperBound;
		Iterator<BAPNode<T, U, V>> iterator = queue.iterator();
		while (iterator.hasNext())
		{
			BAPNode<T, U, V> node = iterator.next();
			lowerBound = Math.min(lowerBound, node.getLowerBound());
			if (node.getLowerBound() >= upperBound)
			{
				iterator.remove();
			}
		}
	}

	public void setLowerBound(double lowerBound)
	{
		this.lowerBound = lowerBound;
	}

	public void setUpperBound(double upperBound)
	{
		if (queue.comparator() instanceof DynamicComparator && this.upperBound == Double.MAX_VALUE)
		{
			// Switch comparator setting.
			@SuppressWarnings("unchecked")
			DynamicComparator<T, U, V> dynamicComparator = (DynamicComparator<T, U, V>) queue.comparator();
			dynamicComparator.performSwitch();

			// Initialise a new priority queue.
			PriorityQueue<BAPNode<T, U, V>> newQueue = new PriorityQueue<>(dynamicComparator);
			newQueue.addAll(queue);
			this.queue = newQueue;
		}
		this.upperBound = upperBound;
	}

	public double getUpperBound()
	{
		return upperBound;
	}

	public double getLowerBound()
	{
		return lowerBound;
	}

	public BAPNode<T, U, V> getLeastCommonAncestor(BAPNode<T, U, V> firstNode, BAPNode<T, U, V> secondNode)
	{
		BAPNode<T, U, V> nodeA = firstNode;
		BAPNode<T, U, V> nodeB = secondNode;

		while (!nodeA.equals(nodeB))
		{
			if (nodeA.getDepth() > nodeB.getDepth())
			{
				nodeA = nodeA.getParent();
			}
			else
			{
				nodeB = nodeB.getParent();
			}
		}
		return nodeA;
	}
}
