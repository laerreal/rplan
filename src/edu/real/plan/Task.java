package edu.real.plan;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;

import edu.real.external.BiMap;
import edu.real.external.ZonedDateTime;

public class Task
{
	String name;
	String descripton;

	/**
	 * Test or sound records, images, etc?
	 */
	Collection<Note> notes;

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
	BiMap<Task, Task> prerequesites;

	/**
	 * Several time references
	 */
	ZonedDateTime creation_timestamp;
	ZonedDateTime finish_timestamp;
	Object deadline;

	/**
	 * Position on a canvas.
	 */
	int x;
	int y;

	/**
	 * Are the notes shown?
	 */
	boolean expanded;

	/**
	 * Unsupported data read from string during loading. Preserved to be saved.
	 * This is forward compatibility.
	 */
	String opaque;

	/**
	 * Allow external objects to track this task changes.
	 */
	Collection<TaskListener> listeners;

	public Task()
	{
		this.expanded = true;
		this.notes = new LinkedList<Note>();
		listeners = new LinkedList<TaskListener>();
	}

	public void save(StringWriter w)
	{
		w.write("name " + this.name + "\n");
		w.write("description " + this.descripton.replaceAll("\\", "\\\\")
				.replaceAll("\n", "\\n") + "\n");
		w.write("notes\n"); // TODO: how?
		w.write(String.format("color 0x%08x\n", this.color));
		w.write("icon\n"); // TODO: save a URL?
		w.write("creation_timestamp " + this.creation_timestamp.toString()
				+ "\n");
		w.write("finish_timestamp " + this.finish_timestamp.toString() + "\n");
		w.write("deadline\n"); // TODO: a id of deadline?
		w.write(String.format("x %i\n", this.x));
		w.write(String.format("y %i\n", this.y));
		if (this.opaque != null) {
			w.write(this.opaque);
		}
	}

	public void load(String[] lines)
			throws ParseException, IllegalAccessException,
			IllegalArgumentException
	{
		String opaque = "";
		for (String l : lines) {
			final String fieldnName;
			final String fieldValue;
			int i = l.indexOf(" ");
			if (i < 0) {
				fieldnName = l;
				fieldValue = null;
			} else {
				fieldnName = l.substring(0, i);
				fieldValue = l.substring(i + 1);
			}
			if (fieldnName == "opaque") {
				// cannot load "opaque" explicitly
				continue;
			}
			final Field f;
			try {
				f = this.getClass().getDeclaredField(fieldnName);
			} catch (NoSuchFieldException e) {
				opaque += l + "\n";
				continue;
			}
			final Object val;
			if (fieldValue == null) {
				val = null;
			} else if (fieldnName == "description") {
				val = fieldValue.replace("\\n", "\n").replace("\\\\", "\\");
			} else if (fieldnName == "notes") {
				val = null; // TODO
			} else if (fieldnName == "color") {
				val = Integer.parseInt(fieldValue);
			} else if (fieldnName == "icon") {
				val = null; // TODO
			} else if (fieldnName == "creation_timestamp" ||
					fieldnName == "finish_timestamp") {
				val = ZonedDateTime.parse(fieldValue);
			} else if (fieldnName == "deadline") {
				val = null; // TODO
			} else if (fieldnName == "x" || fieldnName == "y") {
				val = Integer.parseInt(fieldValue);
			} else {
				val = fieldValue;
			}

			f.set(this, val);
		}
		if (opaque == "") {
			this.opaque = null;
		} else {
			this.opaque = opaque;
		}
	}

	public void addListener(TaskListener l)
	{
		listeners.add(l);
	}

	public int getX()
	{
		return this.x;
	}

	public int getY()
	{
		return this.y;
	}

	public void move(int x, int y)
	{
		this.x = x;
		this.y = y;
		for (TaskListener l : listeners) {
			l.onMove(this);
		}
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
		for (TaskListener l : listeners) {
			l.onRename(this);
		}
	}

	public boolean isExpanded()
	{
		return this.expanded;
	}

	public void addNote(Note note)
	{
		this.notes.add(note);
	}

	public Collection<Note> getNotes()
	{
		return new LinkedList<Note>(this.notes);
	}

	public String getDescription()
	{
		return descripton;
	}

	public void setDescription(String desc)
	{
		descripton = desc;
	}
}
