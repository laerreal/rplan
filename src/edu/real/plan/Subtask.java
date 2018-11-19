package edu.real.plan;

import java.io.StringWriter;

public class Subtask extends TextNote
{
	public static final String CHECKED_PREFIX = "checked ";
	public static final String UNCHECKED_PREFIX = "unchecked ";

	boolean checked;

	public Subtask(boolean _checked)
	{
		super();
		checked = _checked;
	}

	public Subtask(String text, boolean _checked)
	{
		this(text, _checked, 0);
	}

	public Subtask(String text, boolean _checked, int _indent)
	{
		super(text, _indent);
		checked = _checked;
	}

	public Subtask(String text)
	{
		this(text, false);
	}

	public Subtask()
	{
		this(false);
	}

	public boolean getChecked()
	{
		return checked;
	}

	public void setChecked(boolean _checked)
	{
		checked = _checked;
		notifyChanged();
	}

	@Override
	public void save(StringWriter w)
	{
		w.write((checked ? CHECKED_PREFIX : UNCHECKED_PREFIX) + text);
	}
}
