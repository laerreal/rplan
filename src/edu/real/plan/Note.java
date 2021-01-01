package edu.real.plan;

import java.io.StringWriter;

import edu.real.external.Notifier;

public abstract class Note extends Notifier<NoteListener>
{
	private int indent;

	public Note(int _indent)
	{
		indent = _indent;
	}

	protected void notifyChanged()
	{
		for (begin(); next(); l.onChanged(this))
			;
	}

	public abstract void save(StringWriter w);

	public static final Note load(String line)
	{
		int indent = 0;
		for (int I = line.length(); indent < I
				&& line.charAt(indent) == ' '; indent++)
			;
		if (indent > 0) {
			line = line.substring(indent);
		}
		if (line.startsWith(TextNote.PREFIX)) {
			return new TextNote(line.substring(TextNote.PREFIX.length()),
					indent);
		} else if (line.startsWith(Subtask.CHECKED_PREFIX)) {
			return new Subtask(line.substring(Subtask.CHECKED_PREFIX.length()),
					true, indent);
		} else if (line.startsWith(Subtask.UNCHECKED_PREFIX)) {
			return new Subtask(
					line.substring(Subtask.UNCHECKED_PREFIX.length()),
					false, indent);
		}
		return null;
	}

	public void setIndent(int i)
	{
		if (i == indent)
			return;

		for (begin(); next(); l.onIndenting(this, i))
			;

		indent = i;
	}

	public int getIndent()
	{
		return indent;
	}

	public void saveIndent(StringWriter w)
	{
		for (int i = 0; i < indent; i++) {
			w.write(' ');
		}
	}

	public static String preprocessHTML(String html)
	{
		String lines[] = html.split("[\n\r]+");
		String ret = "";
		for (String l : lines) {
			ret = ret + l;
		}
		return ret;
	}
}
