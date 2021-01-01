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
		for (int I = line.length(); indent < I; indent++) {
			char c = line.charAt(indent);
			if (!Character.isWhitespace(c)) {
				break;
			}
		}

		if (indent > 0) {
			line = line.substring(indent);
		}

		String text;

		text = stripPrefix(TextNote.PREFIX, line);
		if (text != null) {
			return new TextNote(text, indent);
		}

		text = stripPrefix(Subtask.CHECKED_PREFIX, line);
		if (text != null) {
			return new Subtask(text, true, indent);
		}

		text = stripPrefix(Subtask.UNCHECKED_PREFIX, line);
		if (text != null) {
			return new Subtask(text, false, indent);
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

	static String stripPrefix(String prefix, String line)
	{
		final int prefix_length = prefix.length();
		if (line.startsWith(prefix)) {
			if (line.length() == prefix_length) {
				return "";
			} else {
				char sep = line.charAt(prefix_length);
				if (Character.isWhitespace(sep)) {
					return line.substring(prefix_length + 1);
				} else {
					return null;
				}
			}
		} else {
			return null;
		}
	}
}
