package edu.real.plan;

import edu.real.external.Notifier;

public abstract class Note extends Notifier<NoteListener>
{
	protected void notifyChanged() {
		for (begin(); next(); l.onChanged(this))
			;
	}
}
