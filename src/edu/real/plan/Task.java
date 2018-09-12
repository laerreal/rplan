package edu.real.plan;

import java.util.Collection;

import edu.real.external.BiMap;

public class Task
{
	String name;
	String descripton;

	/**
	 * Test or sound records, images, etc?
	 */
	Collection<Object> notes;

	/**
	 * A simple graphical representation.
	 */
	int color;

	/**
	 * An image representing the task.
	 */
	Object icon;

	/**
	 * Tasks this one does depends on.
	 */
	BiMap<Task> prerequesites;

	/**
	 * Several time references
	 */
	Object creation_timestamp;
	Object finish_timestamp;
	Object deadline;

	/**
	 * Position on a canvas.
	 */
	float x, y;
}
