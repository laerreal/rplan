package edu.real.plan;

public class TextNote implements Note
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
		return this.text;
	}

	public void setText(String text)
	{
		this.text = text;
	}
}
