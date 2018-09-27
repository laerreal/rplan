package edu.real.android.plan;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import edu.real.plan.Task;

public class TaskViewListener implements OnTouchListener
{
	public final int DRAG_THRESHOLD = 10;
	int x, y;
	boolean dragging;
	boolean pressed;
	Task task;
	TaskViewer viewer;

	public TaskViewListener(TaskViewer tv, Task t)
	{
		dragging = false;
		pressed = false;
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
				pressed = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (viewer.getMode() == TaskViewer.MODE_MOVE) {
				final int dx = X - x;
				final int dy = Y - y;
				if (pressed) {
					if (Math.abs(dx) > DRAG_THRESHOLD ||
							Math.abs(dy) > DRAG_THRESHOLD) {
						dragging = true;
						pressed = false;
					}
				}
				if (dragging) {
					x = X;
					y = Y;
					task.move(task.getX() + dx, task.getY() + dy);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (dragging) {
				dragging = false;
			} else if (pressed) {
				// Touched
				pressed = false;
				viewer.editTask(task);
			}
		}
		return true;
	}

}
