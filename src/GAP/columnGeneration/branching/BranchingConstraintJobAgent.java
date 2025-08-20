package GAP.columnGeneration.branching;

import GAP.GAPInstance;
import GAP.columnGeneration.AssignmentColumn;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class BranchingConstraintJobAgent extends AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem>
{
	private final int agent;
	private final int job;
	private final boolean allowed;

	public BranchingConstraintJobAgent(int agent, int job, boolean allowed)
	{
		super(ConstraintType.EQUALITY, 0);

		this.agent = agent;
		this.job = job;
		this.allowed = allowed;
	}

	@Override
	public boolean containsColumn(GAPColumn column)
	{
		if (column instanceof AssignmentColumn)
		{
			AssignmentColumn assignmentColumn = (AssignmentColumn) column;
			if (assignmentColumn.getAgent() == agent)
			{
				if (allowed)
				{
					// If the column does not contain the job, it may not be used.
					return !assignmentColumn.getJobs().contains(job);
				}
				else
				{
					// If the column contains the job, it may not be used.
					return assignmentColumn.getJobs().contains(job);
				}
			}
			else
			{
				if (allowed)
				{
					// The job must be assigned to a different agent, so if the column contains the job it may not be used.
					return assignmentColumn.getJobs().contains(job);
				}
				else
				{
					// We cannot make any statement here.
					return false;
				}
			}
		}
		return false;
	}
	
	@Override
	public void addSlackVariable(
			AbstractMasterProblem<GAPInstance, GAPColumn, GAPPricingProblem> masterProblem)
			throws IloException
	{
		// Do nothing.
	}

	@Override
	public double getCoefficient(GAPColumn column)
	{
		return 1;
	}

	@Override
	public void updateGenericDuals(GAPInstance instance, double dual)
	{
		// Do nothing.
	}

	@Override
	public void updatePricingProblemDuals(GAPPricingProblem pricingProblem, double dual)
	{
		// Do nothing.
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + agent;
		result = prime * result + (allowed ? 1231 : 1237);
		result = prime * result + job;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		BranchingConstraintJobAgent other = (BranchingConstraintJobAgent) obj;
		if (agent != other.agent) return false;
		if (allowed != other.allowed) return false;
		if (job != other.job) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "BranchingConstraintJobAgent [agent=" + agent + ", job=" + job + ", allowed=" + allowed + "]";
	}
}
