package edu.real.plan;

import java.util.Collection;
import java.util.LinkedList;

import edu.real.external.Notifier;

public class Plan extends Notifier<PlanListener>
{
	Collection<Task> tasks;
	Task current_task;

	public Plan()
	{
		tasks = new LinkedList<Task>();
	}

	public void addTask(Task task)
	{
		tasks.add(task);
		for (begin(); next(); l.onTaskAdded(this, task))
			;
	}

	public Collection<Task> getTasks()
	{
		return new LinkedList<Task>(tasks);
	}

	public Task getCurrentTask()
	{
		return current_task;
	}

	public void setCurrentTask(Task t)
	{
		current_task = t;
	}

	public void removeTask(Task task)
	{
		for (begin(); next(); l.onTaskRemoving(this, task))
			;
		tasks.remove(task);
	}
}
