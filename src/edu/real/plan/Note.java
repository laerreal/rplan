package edu.real.plan;

import java.util.Collection;
import java.util.LinkedList;

public abstract class Note
{
	Collection<NoteListener> listeners;

	protected Note()
	{
		listeners = new LinkedList<NoteListener>();
	}

	public void addListener(NoteListener l)
	{
		listeners.add(l);
	}

	protected void notifyChanged() {
		for (NoteListener l : listeners) {
			l.onChanged(this);
		}
	}
}
