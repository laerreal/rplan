package edu.real.plan;

public interface TaskListener
{
	public void onMove(Task t);

	public void onRename(Task task);

	public void onNoteChanged(Task t, Note n);

	public void onNoteAdded(Task t, Note n);

	public void onNoteRemoving(Task t, Note n);

	public void onNoteMoving(Task t, Note n, int idx);

	public void onCollapsedChanged(Task t, boolean colapsed);
}
