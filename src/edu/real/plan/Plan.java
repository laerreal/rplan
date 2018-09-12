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
}
