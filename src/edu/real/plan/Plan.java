package edu.real.plan;

import java.util.Collection;
import java.util.LinkedList;

public class Plan
{
	Collection<Task> tasks;

	public Plan()
	{
		this.tasks = new LinkedList<Task>();
	}

	public void addTask(Task task)
	{
		this.tasks.add(task);
	}

	public Collection<Task> getTasks()
	{
		return new LinkedList<Task>(this.tasks);
	}
}
