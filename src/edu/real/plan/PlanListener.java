package edu.real.plan;

public interface PlanListener
{
	public void onTaskAdded(Plan p, Task t);

	public void onTaskRemoving(Plan p, Task t);

	public void onViewDragged(Plan p);

	public void onCurrentTaskSet(Plan o, Task t);
}
