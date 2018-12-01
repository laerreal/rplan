package edu.real.android.plan;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import edu.real.android.plan.shortcuts.TLParams;
import edu.real.android.plan.shortcuts.TaskLayout;
import edu.real.external.BiMap;
import edu.real.external.CF;
import edu.real.external.ZonedDateTime;
import edu.real.plan.Note;
import edu.real.plan.Plan;
import edu.real.plan.PlanListener;
import edu.real.plan.Subtask;
import edu.real.plan.Task;
import edu.real.plan.TaskListener;
import edu.real.plan.TextNote;

public class TaskViewer
		implements Callback, OnTouchListener, PlanListener, TaskListener
{
	/* task name will appear right under user's finger */
	static final int TASK_CREATION_OFFSET_X = -40;
	static final int TASK_CREATION_OFFSET_Y = -60;

	static final int TASK_CREATION_THRESHOLD = 40; // a Manchester distance

	static final String TAG_NAME = "name";
	static final String TAG_CHECKBOX = "checkbox";

	static final int MODE_MOVE = 1;

	Plan plan;
	SurfaceView backpane;
	RelativeLayout pane;
	Context pane_context;
	SharedPreferences prefs;
	BiMap<Task, TaskLayout> task2view;
	Map<TaskLayout, BiMap<Note, View>> taskview2noteviews;
	int mode;
	SurfaceHolder holder;

	int down_x;
	int down_y;

	float task_title_font_scale;

	public TaskViewer(RelativeLayout pane)
	{
		plan = null;
		this.pane = pane;
		// TODO: lookup by a class or a tag
		backpane = (SurfaceView) pane.findViewById(R.id.backpane);
		pane_context = pane.getContext();
		task2view = new BiMap<Task, TaskLayout>();
		holder = backpane.getHolder();
		holder.addCallback(this);
		taskview2noteviews = new HashMap<TaskLayout, BiMap<Note, View>>();
		mode = MODE_MOVE;
		backpane.setOnTouchListener(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(pane_context);
	}

	public void init()
	{
		String tmp = prefs.getString("pref_taskTitleFontScale", "1.5");
		try {
			task_title_font_scale = Float.parseFloat(tmp);
		} catch (NumberFormatException e) {
			task_title_font_scale = 1.5f;
		}
		for (Task task : plan.getTasks()) {
			initTaskView(task);
		}
	}

	public void deinit()
	{
		for (Task task : task2view.keys()) {
			task.removeListener(this);
			pane.removeView(task2view.get(task));
		}

		/* There are several GUI listeners those keep reference to this
		 * TaskViewer instance. But they are not expected to be called because
		 * all those views are removed. */

		task2view.clear();
		taskview2noteviews.clear();
	}

	private TaskLayout initTaskView(Task task)
	{
		TaskLayout tl = initTask(task);
		pane.addView(tl);
		task2view.put(task, tl);
		tl.setOnTouchListener(new TaskViewListener(this, task));
		task.addListener(this);
		return tl;
	}

	void updateTask(TaskLayout tl, Task t)
	{
		tl.measure(1000, 1000);
		int w = tl.getMeasuredWidth();
		int h = tl.getMeasuredHeight();
		int x = t.getX();
		int y = t.getY();

		LayoutParams lp = new LayoutParams(w, h);
		lp.leftMargin = x;
		lp.topMargin = y;
		// Move the right (bottom) margin beyond the container at least for
		// width (height) of that task's view (with 1 extra pixel for
		// sureness).
		lp.rightMargin = -w - 1;
		lp.bottomMargin = -h - 1;

		tl.setLayoutParams(lp);
	}

	public void update(Plan new_plan)
	{
		if (new_plan != plan) {
			if (plan != null) {
				plan.removeListener(this);
				deinit();
			}
			plan = new_plan;
			if (plan == null) {
				return;
			}
			plan.addListener(this);
			init();
		}

		for (TaskLayout tl : this.task2view.values()) {
			Task t = this.task2view.getKey(tl);

			updateTaskNotes(tl, t);
			updateTask(tl, t);
		}

		tryDrawing(holder);
	}

	private void updateTaskNotes(TaskLayout tl, Task t)
	{
		BiMap<Note, View> noteviews = this.taskview2noteviews.get(tl);
		if (t.isExpanded()) {
			if (noteviews == null) {
				noteviews = new BiMap<Note, View>();
				this.taskview2noteviews.put(tl, noteviews);

				for (Note note : t.getNotes()) {
					View nv = this.initNote(note);

					tl.addView(nv);
					noteviews.put(note, nv);
				}
			}
		} else {
			if (noteviews != null) {
				// Collapsed, remove views
				for (View nv : noteviews.values()) {
					tl.removeView(nv);
				}
				this.taskview2noteviews.put(tl, null);
			}
		}
	}

	private View initNote(Note note)
	{
		final View ret;
		if (note instanceof Subtask) {
			LinearLayout ll = (LinearLayout) (ret = new LinearLayout(
					pane_context));
			ll.setOrientation(LinearLayout.HORIZONTAL);
			CheckBox cb = new CheckBox(pane_context);
			cb.setTag(TAG_CHECKBOX);
			cb.setOnCheckedChangeListener(
					new SubtaskCheckedListener((Subtask) note));
			ll.addView(cb);

			/* Some margin correction is required. */
			/* XXX: it could be device-specific */
			TLParams lp;
			lp = (TLParams) cb.getLayoutParams();
			lp.setMargins(lp.leftMargin - 5, lp.topMargin - 5, lp.rightMargin,
					lp.bottomMargin - 5);
			cb.setLayoutParams(lp);

			TextView tv = new TextView(pane_context);
			tv.setTag(TAG_NAME);
			ll.addView(tv);

			/* Some margin correction is required. */
			/* XXX: it could be device-specific */
			lp = (TLParams) tv.getLayoutParams();
			lp.setMargins(lp.leftMargin, lp.topMargin - 5, lp.rightMargin,
					lp.bottomMargin);
			tv.setLayoutParams(lp);

			updateNoteView(note, ret);
		} else if (note instanceof TextNote) {
			ret = new TextView(pane_context);
			updateNoteView(note, ret);
		} else {
			TextView tv = (TextView) (ret = new TextView(this.pane_context));
			tv.setText("Unknown class of note! " + note.getClass().getName());
		}
		return ret;
	}

	public TaskLayout initTask(Task task)
	{
		TaskLayout l = new TaskLayout(this.pane_context);

		TextView tv_name = new TextView(this.pane_context);
		tv_name.setTag(TAG_NAME);
		tv_name.setTextSize(tv_name.getTextSize() * task_title_font_scale);
		updateTaskName(task, tv_name);
		l.addView(tv_name);

		return l;
	}

	private void updateTaskName(Task task, TextView tv_name)
	{
		String name = task.getName();

		if (name.trim().length() == 0) {
			name = "<blank>";
		}

		tv_name.setText(name);
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
		updateTaskName(task, tv);
	}

	public void editTask(Task task)
	{
		plan.setCurrentTask(task);
		Context ctx = pane.getContext();
		if (CF.DEBUG < 1)
			Log.v("editTask", "starting");
		ctx.startActivity(new Intent(ctx, TaskEditActivity.class));
		if (CF.DEBUG < 1)
			Log.v("editTask", "started");
	}

	@Override
	public void onNoteChanged(Task t, Note n)
	{
		if (!t.isExpanded()) {
			return;
		}
		BiMap<Note, View> notes = (BiMap<Note, View>) taskview2noteviews
				.get(task2view.get(t));

		View nv = notes.get(n);

		updateNoteView(n, nv);
	}

	@Override
	public void onNoteIndenting(Task t, Note n, int new_indent)
	{
		// TODO Auto-generated method stub

	}

	protected void updateNoteView(Note n, View nv)
	{
		if (n instanceof Subtask) {
			Subtask subtask = (Subtask) n;
			TextView tv = (TextView) nv.findViewWithTag(TAG_NAME);
			CheckBox cb = (CheckBox) nv.findViewWithTag(TAG_CHECKBOX);
			tv.setText(subtask.getText());
			cb.setChecked(subtask.getChecked());
		} else if (n instanceof TextNote) {
			TextNote tn = (TextNote) n;
			TextView tv = (TextView) nv;
			tv.setText(tn.getText());
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		final int X = (int) event.getX();
		final int Y = (int) event.getY();

		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			down_x = X;
			down_y = Y;
			break;
		case MotionEvent.ACTION_UP:
			int dx = X - down_x;
			int dy = Y - down_y;
			if (Math.abs(dx) + Math.abs(dy) > TASK_CREATION_THRESHOLD) {
				break;
			}
			Task t = new Task(X + TASK_CREATION_OFFSET_X,
					Y + TASK_CREATION_OFFSET_Y);
			plan.addTask(t);
			editTask(t);
			break;
		}
		return true;
	}

	@Override
	public void onTaskAdded(Plan p, Task t)
	{
		View v = initTaskView(t);
		updateTaskNotes(v, t);
		updateTask(v, t);
	}

	@Override
	public void onNoteAdded(Task t, Note n)
	{
		if (!t.isExpanded()) {
			return;
		}

		ViewGroup vg = (ViewGroup) task2view.get(t);
		BiMap<Note, View> noteviews = taskview2noteviews.get(vg);

		View nv = initNote(n);

		vg.addView(nv);
		noteviews.put(n, nv);
	}

	@Override
	public void onNoteRemoving(Task t, Note n)
	{
		if (!t.isExpanded()) {
			return;
		}

		ViewGroup vg = (ViewGroup) task2view.get(t);
		BiMap<Note, View> noteviews = taskview2noteviews.get(vg);
		View v = noteviews.pop(n);
		vg.removeView(v);
	}

	public void removeTask(Task task)
	{
		plan.removeTask(task);
	}

	@Override
	public void onTaskRemoving(Plan p, Task t)
	{
		View v = task2view.pop(t);
		pane.removeView(v);
	}

	@Override
	public void onNoteMoving(Task t, Note n, int idx)
	{
		if (!t.isExpanded()) {
			return;
		}

		ViewGroup vg = (ViewGroup) task2view.get(t);
		BiMap<Note, View> noteviews = taskview2noteviews.get(vg);
		View nv = noteviews.get(n);
		int offset = vg.indexOfChild(nv) - t.getNoteIndex(n);
		vg.removeView(nv);
		vg.addView(nv, idx + offset);
	}

	@Override
	public void onCollapsedChanged(Task t, boolean colapsed)
	{
		View v = task2view.get(t);
		updateTaskNotes(v, t);
		// Because of task's content is likely changed, its layout also must
		// be updated.
		updateTask(v, t);
	}

	@Override
	public void onCreationTSChanging(Task t, ZonedDateTime ts)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onLastEditedTSChanging(Task t, ZonedDateTime ts)
	{
		// TODO Auto-generated method stub

	}
}
