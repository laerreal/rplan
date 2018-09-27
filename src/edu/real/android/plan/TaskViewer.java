package edu.real.android.plan;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import edu.real.external.BiMap;
import edu.real.plan.Note;
import edu.real.plan.Plan;
import edu.real.plan.Task;
import edu.real.plan.TaskListener;
import edu.real.plan.TextNote;

public class TaskViewer implements Callback, TaskListener
{
	protected static final String TAG_NAME = "name";

	static final int MODE_MOVE = 1;

	Plan plan;
	SurfaceView backpane;
	RelativeLayout pane;
	Context pane_context;
	BiMap<Task, View> task2view;
	Map<View, Collection<View>> taskview2noteviews;
	int mode;
	SurfaceHolder holder;

	public TaskViewer(Plan plan, RelativeLayout pane)
	{
		this.plan = plan;
		this.pane = pane;
		// TODO: lookup by a class or a tag
		this.backpane = (SurfaceView) pane.findViewById(R.id.backpane);
		this.pane_context = pane.getContext();
		this.task2view = new BiMap<Task, View>();
		holder = backpane.getHolder();
		holder.addCallback(this);
		this.taskview2noteviews = new HashMap<View, Collection<View>>();
		this.mode = MODE_MOVE;
	}

	public void init()
	{
		for (Task task : this.plan.getTasks()) {
			View v = this.initTask(task);
			this.pane.addView(v);
			this.task2view.put(task, v);
			v.setOnTouchListener(new TaskViewListener(this, task));
			task.addListener(this);
		}
	}

	void updateTask(View v, Task t)
	{
		v.measure(1000, 1000);
		int w = v.getMeasuredWidth();
		int h = v.getMeasuredHeight();
		int x = t.getX();
		int y = t.getY();

		LayoutParams lp = new LayoutParams(w, h);
		lp.leftMargin = x;
		lp.topMargin = y;
		v.setLayoutParams(lp);
	}

	public void update()
	{
		for (View v : this.task2view.values()) {
			Task t = this.task2view.getKey(v);

			Collection<View> noteviews = this.taskview2noteviews.get(v);
			if (t.isExpanded()) {
				if (noteviews == null) {
					noteviews = new LinkedList<View>();
					this.taskview2noteviews.put(v, noteviews);
					ViewGroup vg = (ViewGroup) v;

					for (Note note : t.getNotes()) {
						View nv = this.initNote(note);

						vg.addView(nv);
					}
				}
			} else {
				if (noteviews != null) {
					// Collapsed, remove views
					ViewGroup vg = (ViewGroup) v;
					for (View nv : noteviews) {
						vg.removeView(nv);
					}
					this.taskview2noteviews.put(v, null);
				}
			}

			updateTask(v, t);
		}

		tryDrawing(holder);
	}

	private View initNote(Note note)
	{
		final View ret;
		if (note instanceof TextNote) {
			TextView tv = (TextView) (ret = new TextView(this.pane_context));
			tv.setText(((TextNote) note).getText());
		} else {
			TextView tv = (TextView) (ret = new TextView(this.pane_context));
			tv.setText("Unknown class of note! " + note.getClass().getName());
		}
		return ret;
	}

	public View initTask(Task task)
	{
		LinearLayout l = new LinearLayout(this.pane_context);
		l.setOrientation(LinearLayout.VERTICAL);

		TextView tv_name = new TextView(this.pane_context);
		tv_name.setTag(TAG_NAME);
		tv_name.setTextSize(tv_name.getTextSize() * 1.5f);
		tv_name.setText(task.getName());
		l.addView(tv_name);

		return l;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		tryDrawing(holder);
	}

	@Override
	public void surfaceChanged(
			SurfaceHolder holder, int format, int width, int height
	)
	{
		tryDrawing(holder);
	}

	private void tryDrawing(SurfaceHolder holder)
	{
		Canvas canvas = holder.lockCanvas();
		if (canvas != null) {
			drawMyStuff(canvas);
			holder.unlockCanvasAndPost(canvas);
		}
	}

	private void drawMyStuff(final Canvas canvas)
	{
		canvas.drawRGB(255, 255, 255);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onMove(Task t)
	{
		updateTask(task2view.get(t), t);
	}

	public int getMode()
	{
		return mode;
	}

	@Override
	public void onRename(Task task)
	{
		TextView tv = (TextView) task2view.get(task).findViewWithTag(TAG_NAME);
		tv.setText(task.getName());
	}

	public void editTask(Task task)
	{
		plan.setCurrentTask(task);
		Context ctx = pane.getContext();
		ctx.startActivity(new Intent(ctx, TaskEditActivity.class));
	}
}
