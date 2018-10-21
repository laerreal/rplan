package edu.real.plan;

import java.io.StringWriter;

public class TextNote extends Note
{
	public static final String PREFIX = "text ";

	String text;

	public TextNote()
	{
		this("");
	}

	public TextNote(String text)
	{
		this.text = text;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
		notifyChanged();
	}

	@Override
	public void save(StringWriter w)
	{
		w.write(PREFIX + text);
	}
}
