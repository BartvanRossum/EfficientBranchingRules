package optimisation.BAP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import ilog.concert.IloException;
import optimisation.BAP.strongBranching.StrongBranching;
import optimisation.columnGeneration.AbstractColumn;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractInstance;
import optimisation.columnGeneration.AbstractMasterProblem;
import optimisation.columnGeneration.AbstractSolution;
import optimisation.columnGeneration.ColumnGeneration;
import optimisation.columnGeneration.pricing.AbstractPricingProblem;
import optimisation.cuts.AbstractCutSeparator;
import optimisation.cuts.GenericBranchingDecision;
import util.Configuration;
import util.Logger;
import util.Logger.CountQuantity;
import util.Logger.TimeQuantity;
import util.Logger.ValueQuantity;

public class BranchAndPrice<T extends AbstractInstance, U extends AbstractColumn<T, V>, V extends AbstractPricingProblem<T>>
{
	private final BranchingTree<T, U, V> branchingTree;
	private List<AbstractBranchingRule<T, U, V>> branchingRules;
	private List<AbstractCutSeparator<T, U, V>> cutSeparators;

	private final T instance;
	private final AbstractMasterProblem<T, U, V> masterProblem;
	private final ColumnGeneration<T, U, V> columnGeneration;

	private AbstractSolution<T, U, V> bestSolution;

	private long timeLimit = Long.MAX_VALUE;

	public BranchAndPrice(Comparator<BAPNode<T, U, V>> comparator, T instance,
			AbstractMasterProblem<T, U, V> masterProblem, ColumnGeneration<T, U, V> columnGeneration)
	{
		this.branchingTree = new BranchingTree<T, U, V>(comparator);
		this.branchingRules = new ArrayList<>();
		this.cutSeparators = new ArrayList<>();

		this.instance = instance;
		this.masterProblem = masterProblem;
		this.columnGeneration = columnGeneration;

		this.bestSolution = null;
	}

	public void applyBranchAndPrice() throws IloException
	{
		// Retrieve logger.
		Logger logger = Logger.getLogger();

		// Initialise a root node and last processed node.
		BAPNode<T, U, V> rootNode = new BAPNode<T, U, V>(null);
		BAPNode<T, U, V> previousNode = rootNode;

		// Keep track of performance.
		double previousUB = branchingTree.getUpperBound();
		double previousGap = Double.MAX_VALUE;
		logger.resetNode();

		// Add root node to the tree.
		branchingTree.enqueue(rootNode);

		while (!branchingTree.isEmpty())
		{
			// Process the first node from the queue.
			BAPNode<T, U, V> parent = branchingTree.dequeue();

			// Process branching decisions.
			processBranchingDecisions(previousNode, parent);

			// Determine whether we are in enumeration mode.
			boolean enumerating = parent.getPotentialColumns() != null;
			int nodeIndex = Logger.getLogger().getNode();

			// Generate columns and cuts until optimality.
			boolean go = true;
			while (go)
			{
				// Solve this node with column generation.
				go = false;
				
				// We can terminate column generation at the primal lower bound.
				double lowerBound = parent.getLowerBound();
				columnGeneration.applyColumnGeneration(masterProblem, instance, lowerBound);

				// Prune the node if it is infeasible.
				if (!masterProblem.isFeasible())
				{
					break;
				}

				// Separate cuts, if any.
				boolean separatedCuts = false;
				for (AbstractCutSeparator<T, U, V> cutSeparator : cutSeparators)
				{
					if (!cutSeparator.separate(nodeIndex, enumerating))
					{
						continue;
					}

					// Add cuts to the model.
					logger.startTimer(TimeQuantity.TIME_SEPARATING);
					Set<AbstractConstraint<T, U, V>> cuts = cutSeparator.generateCuts(masterProblem);
					if (cuts.size() > 0)
					{
						separatedCuts = true;
						go = true;
					}
					logger.incrementCount(CountQuantity.NUM_SEPARATED_CUT, cuts.size());
					for (AbstractConstraint<T, U, V> cut : cuts)
					{
						if (cutSeparator.isGloballyValid())
						{
							masterProblem.addConstraintWithExistingColumns(cut, masterProblem.getColumns());
						}
						else
						{
							GenericBranchingDecision<T, U, V> decision = new GenericBranchingDecision<>(cut);
							parent.addBranchingDecision(decision);
							masterProblem.processBranchingDecision(decision);
						}
					}
					logger.stopTimer(TimeQuantity.TIME_SEPARATING);
					if (separatedCuts)
					{
						break;
					}
				}
			}

			// Process the node.
			processNode(parent.equals(rootNode), parent);

			// Update lower bound.
			branchingTree.updateLowerBound();

			// Update bounds and node counter.
			logger.setValue(ValueQuantity.VALUE_LOWER_BOUND, branchingTree.getLowerBound());
			logger.setValue(ValueQuantity.VALUE_UPPER_BOUND, branchingTree.getUpperBound());
			logger.increaseNode();

			// Termination criterion.
			double absoluteGap = Math.max(0, branchingTree.getUpperBound() - branchingTree.getLowerBound());
			double gap = 100.0 * absoluteGap / branchingTree.getUpperBound();
			if (logger.getNode() % 1 == 0 || branchingTree.getUpperBound() != previousUB
					|| Math.abs(gap - previousGap) > 1)
			{
				System.out.println("LB: " + branchingTree.getLowerBound() + ". UB:" + branchingTree.getUpperBound()
						+ ". Gap: " + gap + "%. Node: " + logger.getNode() + ". Time (ms): " + logger.getTime()
						+ ". Nodes: " + branchingTree.getNumberOfNodes());
			}
			if (absoluteGap < Configuration.getConfiguration().getDoubleProperty("PRECISION"))
			{
				System.out.println("LB: " + branchingTree.getLowerBound() + ". UB:" + branchingTree.getUpperBound()
						+ ". Gap: " + gap + "%. Time (ms): " + logger.getTime());
				break;
			}

			// Terminate if time limit has been reached.
			if (logger.getTime() >= timeLimit)
			{
				System.out.println("Terminating due to time limit.");
				break;
			}

			// Update last processed node.
			previousNode = parent;
			previousUB = branchingTree.getUpperBound();
			previousGap = gap;
		}

	}

	private void processNode(boolean isRootNode, BAPNode<T, U, V> parent) throws IloException
	{
		// Deal with infeasibilities.
		if (!masterProblem.isFeasible())
		{
			// The node should be pruned, and no child nodes should be created.
			parent.setLowerBound(Double.MAX_VALUE);
			return;
		}

		// Update the upper and lower bound of this node.
		parent.setSolution(masterProblem.getSolution());
		parent.setLowerBound(masterProblem.getObjectiveValue());

		// If the lower bound exceeds the best incumbent solution, we can terminate
		// early.
		if (parent.getLowerBound() >= branchingTree.getUpperBound())
		{
			return;
		}

		// Determine the first branching rule that applies, if any.
		List<BranchingCandidate<T, U, V>> branchingCandidates = new ArrayList<>();
		boolean canBranch = false;
		Logger.getLogger().startTimer(TimeQuantity.TIME_BRANCHING);
		for (AbstractBranchingRule<T, U, V> rule : branchingRules)
		{
			branchingCandidates = rule.getBranchingCandidates(parent);
			if (branchingCandidates.size() > 0)
			{
				canBranch = true;
				break;
			}
		}
		Logger.getLogger().stopTimer(TimeQuantity.TIME_BRANCHING);

		// When no branching rule applies, the solution forms a valid upper bound.
		if (!canBranch)
		{
			System.out.println("Integral solution with objective " + parent.getLowerBound());
			parent.setUpperBound(parent.getLowerBound());
			if (parent.getUpperBound() < branchingTree.getUpperBound())
			{
				bestSolution = parent.getSolution();
				branchingTree.setUpperBound(parent.getUpperBound());
			}
			return;
		}

		// Complete strong branching procedure.
		Logger.getLogger().startTimer(TimeQuantity.TIME_BRANCHING);
		StrongBranching<T, U, V> strongBranching = new StrongBranching<>();
		BranchingCandidate<T, U, V> candidate = strongBranching.determineBranchingCandidate(branchingCandidates,
				masterProblem, columnGeneration, parent.getLowerBound());
		for (BAPNode<T, U, V> child : strongBranching.getChildren(parent, candidate))
		{
			// Pass lower bound to child node.
			child.setLowerBound(parent.getLowerBound());
			branchingTree.enqueue(child);
		}
		Logger.getLogger().stopTimer(TimeQuantity.TIME_BRANCHING);
	}

	public void addCutSeparator(AbstractCutSeparator<T, U, V> cutSeparator)
	{
		this.cutSeparators.add(cutSeparator);
		Collections.sort(cutSeparators, new CutSeparatorComparator());
	}

	public void addBranchingRule(AbstractBranchingRule<T, U, V> branchingRule)
	{
		branchingRules.add(branchingRule);
		Collections.sort(branchingRules, new BranchingRuleComparator());
	}

	public AbstractSolution<T, U, V> getBestSolution()
	{
		return bestSolution;
	}

	public double getLowerBound()
	{
		return branchingTree.getLowerBound();
	}

	public double getUpperBound()
	{
		return branchingTree.getUpperBound();
	}

	private class BranchingRuleComparator implements Comparator<AbstractBranchingRule<T, U, V>>
	{
		@Override
		public int compare(AbstractBranchingRule<T, U, V> o1, AbstractBranchingRule<T, U, V> o2)
		{
			return Double.compare(o1.priority, o2.priority);
		}
	}

	private class CutSeparatorComparator implements Comparator<AbstractCutSeparator<T, U, V>>
	{
		@Override
		public int compare(AbstractCutSeparator<T, U, V> o1, AbstractCutSeparator<T, U, V> o2)
		{
			return Double.compare(o1.priority, o2.priority);
		}
	}

	private void processBranchingDecisions(BAPNode<T, U, V> previousNode, BAPNode<T, U, V> nextNode) throws IloException
	{
		// Find lowest common ancestor.
		BAPNode<T, U, V> LCA = branchingTree.getLeastCommonAncestor(previousNode, nextNode);

		// Undo decisions on path from previous node to LCA.
		BAPNode<T, U, V> current = previousNode;
		while (!current.equals(LCA))
		{
			for (AbstractBranchingDecision<T, U, V> decision : current.getBranchingDecisions())
			{
				masterProblem.undoBranchingDecision(decision);
			}
			current = current.getParent();
		}

		// Process decisions on path from LCA to next node.
		current = nextNode;
		while (!current.equals(LCA))
		{
			for (AbstractBranchingDecision<T, U, V> decision : current.getBranchingDecisions())
			{
				masterProblem.processBranchingDecision(decision);
			}
			current = current.getParent();
		}
	}

	public void setUpperBound(double upperBound)
	{
		branchingTree.setUpperBound(upperBound);
	}

	/**
	 * 
	 * @param timeLimit Time limit in milliseconds.
	 */
	public void setTimeLimit(long timeLimit)
	{
		this.timeLimit = timeLimit;
	}
}
