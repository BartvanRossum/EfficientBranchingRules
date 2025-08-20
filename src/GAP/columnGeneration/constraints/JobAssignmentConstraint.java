package GAP.columnGeneration.constraints;

import GAP.GAPInstance;
import GAP.columnGeneration.AssignmentColumn;
import GAP.columnGeneration.GAPColumn;
import GAP.columnGeneration.pricing.GAPPricingProblem;
import ilog.concert.IloException;
import optimisation.columnGeneration.AbstractConstraint;
import optimisation.columnGeneration.AbstractMasterProblem;

public class JobAssignmentConstraint extends AbstractConstraint<GAPInstance, GAPColumn, GAPPricingProblem>
{
	private final int job;

	public JobAssignmentConstraint(int job)
	{
		super(ConstraintType.EQUALITY, 1);

		this.job = job;
	}

	public int getJob()
	{
		return job;
	}

	@Override
	public boolean containsColumn(GAPColumn column)
	{
		if (column instanceof AssignmentColumn)
		{
			AssignmentColumn assignmentColumn = (AssignmentColumn) column;
			return assignmentColumn.getJobs().contains(job);
		}
		return false;
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
		pricingProblem.setJobDual(job, dual);
	}

	@Override
	public void addSlackVariable(
			AbstractMasterProblem<GAPInstance, GAPColumn, GAPPricingProblem> masterProblem)
			throws IloException
	{
		double slackCost = 1000;
		masterProblem.addSlackVariable(this, 1, slackCost, 0, 1);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + job;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		JobAssignmentConstraint other = (JobAssignmentConstraint) obj;
		if (job != other.job) return false;
		return true;
	}
}