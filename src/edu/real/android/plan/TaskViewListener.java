package edu.real.android.plan;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import edu.real.plan.Task;

public class TaskViewListener implements OnTouchListener
{
	int x, y;
	boolean dragging;
	Task task;
	TaskViewer viewer;

	public TaskViewListener(TaskViewer tv, Task t)
	{
		dragging = false;
		task = t;
		viewer = tv;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		final int X = (int) event.getRawX();
		final int Y = (int) event.getRawY();

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			if (viewer.getMode() == TaskViewer.MODE_MOVE) {
				x = X;
				y = Y;
				dragging = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (viewer.getMode() == TaskViewer.MODE_MOVE && dragging) {
				final int dx = X - x;
				final int dy = Y - y;
				x = X;
				y = Y;
				task.move(task.getX() + dx, task.getY() + dy);
			}
			break;
		case MotionEvent.ACTION_UP:
			dragging = false;
		}
		return true;
	}

}
