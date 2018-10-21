package edu.real.plan;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import edu.real.external.BiMap;
import edu.real.external.Notifier;
import edu.real.external.ZonedDateTime;

public class Task extends Notifier<TaskListener> implements NoteListener
{
	String name;
	String description;

	/**
	 * Test or sound records, images, etc?
	 */
	List<Note> notes;

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

	public Task(int _x, int _y)
	{
		name = "";
		x = _x;
		y = _y;
		expanded = true;
		notes = new LinkedList<Note>();
	}

	public Task()
	{
		this(0, 0);
	}

	public void save(StringWriter w)
	{
		w.write("name " + name + "\n");
		w.write("description " + description.replaceAll("\\", "\\\\")
				.replaceAll("\n", "\\n") + "\n");
		w.write("notes\n");
		for (Note n : notes) {
			n.save(w);
			w.write("\n");
		}
		w.write(String.format("color 0x%08x\n", color));
		w.write("icon\n"); // TODO: save a URL?
		w.write("creation_timestamp " + creation_timestamp.toString()
				+ "\n");
		w.write("finish_timestamp " + finish_timestamp.toString() + "\n");
		w.write("deadline\n"); // TODO: a id of deadline?
		w.write(String.format("x %i\n", x));
		w.write(String.format("y %i\n", y));
		if (opaque != null) {
			w.write(opaque);
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
			if (fieldnName.equals("opaque")) {
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
			} else if (fieldnName.equals("description")) {
				val = fieldValue.replace("\\n", "\n").replace("\\\\", "\\");
			} else if (fieldnName.equals("notes")) {
				val = null; // TODO
			} else if (fieldnName.equals("color")) {
				val = Integer.parseInt(fieldValue);
			} else if (fieldnName.equals("icon")) {
				val = null; // TODO
			} else if (fieldnName.equals("creation_timestamp") ||
					fieldnName.equals("finish_timestamp")) {
				val = ZonedDateTime.parse(fieldValue);
			} else if (fieldnName.equals("deadline")) {
				val = null; // TODO
			} else if (fieldnName.equals("x") || fieldnName.equals("y")) {
				val = Integer.parseInt(fieldValue);
			} else {
				val = fieldValue;
			}

			f.set(this, val);
		}
		if (opaque.equals("")) {
			this.opaque = null;
		} else {
			this.opaque = opaque;
		}
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
		for (begin(); next(); l.onMove(this))
			;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
		for (begin(); next(); l.onRename(this))
			;
	}

	public boolean isExpanded()
	{
		return this.expanded;
	}

	public void addNote(Note note)
	{
		notes.add(note);
		note.addListener(this);
		for (begin(); next(); l.onNoteAdded(this, note))
			;
	}

	public void removeNote(Note note)
	{
		for (begin(); next(); l.onNoteRemoving(this, note))
			;
		note.removeListener(this);
		notes.remove(note);
	}

	public Collection<Note> getNotes()
	{
		return new LinkedList<Note>(this.notes);
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String desc)
	{
		description = desc;
	}

	@Override
	public void onChanged(Note n)
	{
		for (begin(); next(); l.onNoteChanged(this, n))
			;
	}

	public void moveNote(Note n, int idx)
	{
		for (begin(); next(); l.onNoteMoving(this, n, idx))
			;
		notes.remove(n);
		notes.add(idx, n);
	}

	public int getNoteIndex(Note n)
	{
		return notes.indexOf(n);
	}
}
