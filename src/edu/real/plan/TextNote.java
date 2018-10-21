package edu.real.plan;

public class TextNote extends Note
{
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
}
