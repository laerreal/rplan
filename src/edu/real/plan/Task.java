package edu.real.plan;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import edu.real.cross.Color;
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
	/**
	 * Not all changes do update this timestamp. Appearance specific changes
	 * like expanding or moving (entire task) do not update it. But, changing
	 * or moving a note inside do it.
	 */
	ZonedDateTime last_edited_timestamp;
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
	boolean collapsed;

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
		collapsed = false;
		notes = new LinkedList<Note>();
		color = Color.BLACK;
		creation_timestamp = new ZonedDateTime();
		prerequesites = new BiMap<Task, Task>();
	}

	public Task()
	{
		this(0, 0);
	}

	public void save(StringWriter w)
	{
		w.write("name " + name + "\n");
		if (description != null) {
			w.write("description " + description.replace("\\", "\\\\")
					.replace("\n", "\\n") + "\n");
		}
		if (!notes.isEmpty()) {
			w.write("notes\n");
			for (Note n : notes) {
				n.save(w);
				w.write("\n");
			}
		}
		w.write(String.format("color 0x%08x\n", color));
		if (icon != null) {
			w.write("icon\n"); // TODO: save a URL?
		}
		if (!prerequesites.isEmpty()) {
			w.write("prerequesites\n"); // TODO: how to refer another tasks?
		}
		w.write("creation_timestamp " + creation_timestamp.toString()
				+ "\n");
		if (finish_timestamp != null) {
			w.write("finish_timestamp " + finish_timestamp.toString() + "\n");
		}
		if (deadline != null) {
			w.write("deadline\n"); // TODO: a id of deadline?
		}
		w.write(String.format("x %d\n", x));
		w.write(String.format("y %d\n", y));
		if (collapsed) {
			w.write("collapsed\n");
		}
		if (opaque != null) {
			w.write(opaque);
		}
	}

	public static final Task load(String lines[])
			throws ParseException, IllegalAccessException,
			IllegalArgumentException
	{
		Task ret = new Task();
		String opaque = "";
		int i = 0;
		for (; i < lines.length; i++) {
			String l = lines[i];
			final String fieldnName;
			final String fieldValue;
			int sep = l.indexOf(" ");
			if (sep < 0) {
				fieldnName = l;
				fieldValue = null;
			} else {
				fieldnName = l.substring(0, sep);
				fieldValue = l.substring(sep + 1);
			}
			if (fieldnName.equals("opaque")) {
				// cannot load "opaque" explicitly
				continue;
			}
			final Field f;
			try {
				f = Task.class.getDeclaredField(fieldnName);
			} catch (NoSuchFieldException e) {
				opaque += l + "\n";
				continue;
			}
			final Object val;
			if (fieldnName.equals("description")) {
				val = fieldValue.replace("\\n", "\n").replace("\\\\", "\\");
			} else if (fieldnName.equals("notes")) {
				List<Note> _notes = new LinkedList<Note>();
				Note n;
				// goto next line
				i++;
				while (i < lines.length) {
					l = lines[i];
					n = Note.load(l);
					if (n == null) {
						// non-note line has been met
						i--;
						break;
					}
					_notes.add(n);
					i++;
				}
				val = _notes;
			} else if (fieldnName.equals("color")) {
				val = (int) Long.parseLong(fieldValue.substring(2), 16);
			} else if (fieldnName.equals("icon")) {
				val = null; // TODO
			} else if (fieldnName.equals("creation_timestamp") ||
					fieldnName.equals("finish_timestamp")) {
				val = ZonedDateTime.parse(fieldValue);
			} else if (fieldnName.equals("deadline")) {
				val = null; // TODO
			} else if (fieldnName.equals("x") || fieldnName.equals("y")) {
				val = Integer.parseInt(fieldValue);
			} else if (f.getType() == boolean.class) {
				val = true;
			} else {
				val = fieldValue;
			}

			f.set(ret, val);
		}
		if (opaque.equals("")) {
			ret.opaque = null;
		} else {
			ret.opaque = opaque;
		}

		for (Note n : ret.notes) {
			n.addListener(ret);
		}

		return ret;
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
		last_edited_timestamp = new ZonedDateTime();
	}

	public boolean isExpanded()
	{
		return !collapsed;
	}

	public void setCollapsed(boolean v)
	{
		collapsed = v;
		for (begin(); next(); l.onCollapsedChanged(this, v))
			;
	}

	public void addNote(Note note)
	{
		this.insertNote(-1, note);
	}

	public void insertNote(int index, Note note)
	{
		if (index < 0) {
			index += notes.size() + 1;
		}
		notes.add(index, note);
		note.addListener(this);
		for (begin(); next(); l.onNoteAdded(this, index, note))
			;
		last_edited_timestamp = new ZonedDateTime();
	}

	public void removeNote(Note note)
	{
		for (begin(); next(); l.onNoteRemoving(this, note))
			;
		note.removeListener(this);
		notes.remove(note);
		last_edited_timestamp = new ZonedDateTime();
	}

	public Collection<Note> getNotes()
	{
		return new LinkedList<Note>(this.notes);
	}

	public int getNotesCount()
	{
		return notes.size();
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String desc)
	{
		description = desc;
		last_edited_timestamp = new ZonedDateTime();
	}

	@Override
	public void onChanged(Note n)
	{
		for (begin(); next(); l.onNoteChanged(this, n))
			;
		last_edited_timestamp = new ZonedDateTime();
	}

	@Override
	public void onIndenting(Note n, int new_indent)
	{
		for (begin(); next(); l.onNoteIndenting(this, n, new_indent))
			;
		last_edited_timestamp = new ZonedDateTime();
	}

	public void moveNote(Note n, int idx)
	{
		for (begin(); next(); l.onNoteMoving(this, n, idx))
			;
		notes.remove(n);
		notes.add(idx, n);
		last_edited_timestamp = new ZonedDateTime();
	}

	public int getNoteIndex(Note n)
	{
		return notes.indexOf(n);
	}

	public void setCreationTS(ZonedDateTime t)
	{
		for (begin(); next(); l.onCreationTSChanging(this, t))
			;
		creation_timestamp = t;
	}

	public void setLastEditedTS(ZonedDateTime t)
	{
		for (begin(); next(); l.onLastEditedTSChanging(this, t))
			;
		last_edited_timestamp = t;
	}
}
