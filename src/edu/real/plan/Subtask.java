package edu.real.plan;

public class Subtask extends TextNote
{
	boolean checked;

	public Subtask(boolean _checked)
	{
		super();
		checked = _checked;
	}

	public Subtask(String text, boolean _checked)
	{
		super(text);
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
}
