package edu.real.plan;

import java.io.StringWriter;

import edu.real.external.Notifier;

public abstract class Note extends Notifier<NoteListener>
{
	protected void notifyChanged() {
		for (begin(); next(); l.onChanged(this))
			;
	}

	public abstract void save(StringWriter w);

	public static final Note load(String line)
	{
		if (line.startsWith(TextNote.PREFIX)) {
			return new TextNote(line.substring(TextNote.PREFIX.length()));
		} else if (line.startsWith(Subtask.CHECKED_PREFIX)) {
			return new Subtask(line.substring(Subtask.CHECKED_PREFIX.length()),
					true);
		} else if (line.startsWith(Subtask.UNCHECKED_PREFIX)) {
			return new Subtask(
					line.substring(Subtask.UNCHECKED_PREFIX.length()),
					false);
		}
		return null;
	}
}
