package edu.real.android.plan;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.test.MoreAsserts;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import edu.real.plan.Task;

public class TaskViewListener
		implements OnTouchListener, OnGestureListener, OnDoubleTapListener
{
	public final int DRAG_THRESHOLD = 10;
	int x, y;
	boolean dragging;
	boolean pressed;
	boolean longpressed;
	Task task;
	TaskViewer viewer;
	GestureDetector gd;

	public TaskViewListener(TaskViewer tv, Task t)
	{
		dragging = false;
		pressed = false;
		longpressed = false;
		task = t;
		viewer = tv;
		gd = new GestureDetector(viewer.pane_context, this);
		gd.setOnDoubleTapListener(this);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		final boolean pinned = task.isPinned();

		gd.onTouchEvent(event);

		int action = event.getActionMasked();

		switch (action) {
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_HOVER_EXIT:
		case MotionEvent.ACTION_OUTSIDE:
		case MotionEvent.ACTION_CANCEL:
			if (pinned) {
				viewer.onTouch(v, event);
			}
			viewer.setOverTask(false);
			break;
		default:
			viewer.setOverTask(true);
			if (pinned) {
				viewer.onTouch(v, event);
			}
			break;
		}

		final int X = (int) event.getRawX();
		final int Y = (int) event.getRawY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (viewer.getMode() == TaskViewer.MODE_MOVE) {
				x = X;
				y = Y;
				pressed = true;
				longpressed = false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (viewer.getMode() == TaskViewer.MODE_MOVE) {
				if (!task.isPinned()) {
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
			}
			break;
		case MotionEvent.ACTION_UP:
			/* `dragging` reset could be done during `onSingleTapConfirmed` or
			 * `onDoubleTap` but there are situations none of them is called on
			 * `ACTION_UP`. */
			if (dragging) {
				dragging = false;
			}
			break;
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{
		final boolean pinned = task.isPinned();

		if (dragging
			|| (pinned && viewer.isDragged())) {
			return;
		}

		longpressed = true;
		// prevent dragging during this (below) dialog is being shown
		pressed = false;

		CharSequence[] options = new CharSequence[2];

		Resources res = viewer.pane_context.getResources();

		options[0] =  res.getString(R.string.task_popup_remove);

		if (pinned) {
			options[1] =  res.getString(R.string.task_popup_unpin);
		} else {
			options[1] =  res.getString(R.string.task_popup_pin);
		}

		AlertDialog.Builder builder = new Builder(viewer.pane_context);
		builder.setItems(options, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						switch (which) {
						case 0:
							askRemoveTask();
							break;
						case 1:
							task.setPinned(!pinned);
							break;
						}
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	protected void askRemoveTask()
	{
		AlertDialog.Builder builder = new Builder(viewer.pane_context);
		builder.setMessage(R.string.msg_remove_task_q)
				.setTitle(task.getName())
				.setPositiveButton(R.string.bt_yes, new OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						viewer.removeTask(task);
					}
				})
				.setNegativeButton(R.string.bt_no, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e)
	{
		// Note that this method is called before `onTouch` resets `dragging`.
		if (!dragging && pressed) {
			// Touched
			pressed = false;
			if (!longpressed) {
				viewer.editTask(task);
			}
		}
		return true;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e)
	{
		// Note that this method is called before `onTouch` resets `dragging`.
		if (!dragging && pressed) {
			pressed = false;
			if (!longpressed) {
				task.setCollapsed(task.isExpanded());
			}
		}
		return true;
	}

	/* unused gestures below */

	@Override
	public boolean onDown(MotionEvent e)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(
			MotionEvent e1, MotionEvent e2, float distanceX, float distanceY
	)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(
			MotionEvent e1, MotionEvent e2, float velocityX, float velocityY
	)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
