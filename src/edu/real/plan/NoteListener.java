package edu.real.plan;

public interface NoteListener
{
	public void onChanged(Note n);

	public void onIndenting(Note n, int new_indent);
}
