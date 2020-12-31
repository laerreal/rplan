package edu.real.android.plan;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.TextView;
import edu.real.cross.Intersections;
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
		implements Callback, OnTouchListener, PlanListener, TaskListener {
	private static final int SUBTASK_PADDING_REDUCTION = 20;
	private static final int NOTE_INDENT_STEP = 50;
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
	BiMap<Task, View> task2view;
	Map<View, BiMap<Note, View>> taskview2noteviews;
	int mode;
	SurfaceHolder holder;

	int down_x;
	int down_y;
	boolean dragged;
	int prev_x;
	int prev_y;

	float task_title_font_scale;

	Paint paint_task_frame;
	Paint paint_task_pointer;
	float task_pointer_radius = 10;

	Handler handler;
	TaskViewerSurfaceUpdater updater;
	boolean invalid;

	public TaskViewer(RelativeLayout pane)
	{
		dragged = false;
		plan = null;
		this.pane = pane;
		// TODO: lookup by a class or a tag
		backpane = (SurfaceView) pane.findViewById(R.id.backpane);
		pane_context = pane.getContext();
		task2view = new BiMap<Task, View>();
		holder = backpane.getHolder();
		holder.addCallback(this);
		taskview2noteviews = new HashMap<View, BiMap<Note, View>>();
		mode = MODE_MOVE;
		backpane.setOnTouchListener(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(pane_context);

		paint_task_frame = new Paint();
		paint_task_frame.setColor(Color.GRAY);
		paint_task_frame.setStyle(Paint.Style.STROKE);

		paint_task_pointer = new Paint();
		paint_task_pointer.setColor(Color.GRAY);
		paint_task_pointer.setStyle(Paint.Style.FILL);

		handler = new Handler(Looper.getMainLooper());
		invalid = true;
		updater = new TaskViewerSurfaceUpdater(this);
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

	private View initTaskView(Task task)
	{
		View v = initTask(task);
		v.setTag(new TaskViewTag(v, task));
		pane.addView(v);
		task2view.put(task, v);
		v.setOnTouchListener(new TaskViewListener(this, task));
		task.addListener(this);
		return v;
	}

	void updateTask(View v, Task t)
	{
		TaskViewTag tag = (TaskViewTag) v.getTag();
		tag.updateLayoutParams(plan.getViewOffsetX(), plan.getViewOffsetY());
		invalidate();
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

		for (View v : this.task2view.values()) {
			Task t = this.task2view.getKey(v);

			updateTaskNotes(v, t);
			updateTask(v, t);
		}

		tryDrawing(holder);
	}

	private void updateTaskNotes(View v, Task t)
	{
		BiMap<Note, View> noteviews = this.taskview2noteviews.get(v);
		if (t.isExpanded()) {
			if (noteviews == null) {
				noteviews = new BiMap<Note, View>();
				this.taskview2noteviews.put(v, noteviews);
				ViewGroup vg = (ViewGroup) v;

				for (Note note : t.getNotes()) {
					View nv = this.initNote(note);

					vg.addView(nv);
					noteviews.put(note, nv);

					updateNoteIndent(note, nv, note.getIndent());
				}
			}
		} else {
			if (noteviews != null) {
				// Collapsed, remove views
				ViewGroup vg = (ViewGroup) v;
				for (View nv : noteviews.values()) {
					vg.removeView(nv);
				}
				this.taskview2noteviews.put(v, null);
			}
		}
	}

	private void updateNoteIndent(Note note, View nv, int indent)
	{
		android.widget.LinearLayout.LayoutParams nlp;
		nlp = (android.widget.LinearLayout.LayoutParams) nv
				.getLayoutParams();
		nlp.setMargins(NOTE_INDENT_STEP * indent, nlp.topMargin,
				nlp.rightMargin, nlp.bottomMargin);
		nv.setLayoutParams(nlp);
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
			LinearLayout.LayoutParams lp;
			lp = (android.widget.LinearLayout.LayoutParams) cb
					.getLayoutParams();
			lp.setMargins(lp.leftMargin - SUBTASK_PADDING_REDUCTION,
					lp.topMargin - SUBTASK_PADDING_REDUCTION, lp.rightMargin,
					lp.bottomMargin - SUBTASK_PADDING_REDUCTION);
			cb.setLayoutParams(lp);

			TextView tv = new TextView(pane_context);
			tv.setTag(TAG_NAME);
			ll.addView(tv);

			/* Some margin correction is required. */
			/* XXX: it could be device-specific */
			lp = (android.widget.LinearLayout.LayoutParams) tv
					.getLayoutParams();
			lp.setMargins(lp.leftMargin,
					lp.topMargin - SUBTASK_PADDING_REDUCTION,
					lp.rightMargin, lp.bottomMargin);
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

	public View initTask(Task task)
	{
		LinearLayout l = new LinearLayout(this.pane_context);
		l.setOrientation(LinearLayout.VERTICAL);

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
			SurfaceHolder holder, int format, int width, int height)
	{
		tryDrawing(holder);
	}

	private void tryDrawing(SurfaceHolder holder)
	{
		invalid = false;
		Canvas canvas = holder.lockCanvas();
		if (canvas != null) {
			drawMyStuff(canvas);
			holder.unlockCanvasAndPost(canvas);
		}
	}

	private void drawMyStuff(final Canvas canvas)
	{
		canvas.drawRGB(255, 255, 255);

		Rect cnvRect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());

		for (View v : this.task2view.values()) {
			TaskViewTag tag = (TaskViewTag) v.getTag();
			Rect taskRect = tag.getFrameRect(plan.getViewOffsetX(),
					plan.getViewOffsetY());
			if (Rect.intersects(cnvRect, taskRect)) {
				canvas.drawRect(taskRect, paint_task_frame);
			} else {
				float cnvMidX = cnvRect.exactCenterX();
				float cnvMidY = cnvRect.exactCenterY();
				float taskMidX = taskRect.exactCenterX();
				float taskMidY = taskRect.exactCenterY();

				Point p;

				while (true) { /* Not a loop */
					p = Intersections.lineline(
							cnvMidX, cnvMidY, taskMidX, taskMidY,
							cnvRect.left, cnvRect.top,
							cnvRect.right, cnvRect.top);

					if (p != null) {
						break;
					}

					p = Intersections.lineline(
							cnvMidX, cnvMidY, taskMidX, taskMidY,
							cnvRect.right, cnvRect.top,
							cnvRect.right, cnvRect.bottom);

					if (p != null) {
						break;
					}

					p = Intersections.lineline(
							cnvMidX, cnvMidY, taskMidX, taskMidY,
							cnvRect.right, cnvRect.bottom,
							cnvRect.left, cnvRect.bottom);

					if (p != null) {
						break;
					}

					p = Intersections.lineline(
							cnvMidX, cnvMidY, taskMidX, taskMidY,
							cnvRect.left, cnvRect.bottom,
							cnvRect.left, cnvRect.top);
					break;
				}

				canvas.drawCircle(p.x, p.y, task_pointer_radius,
						paint_task_pointer);
			}
		}
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
		View nv = getNoteView(t, n);
		updateNoteView(n, nv);
	}

	private View getNoteView(Task t, Note n)
	{
		BiMap<Note, View> notes = (BiMap<Note, View>) taskview2noteviews
				.get(task2view.get(t));

		View nv = notes.get(n);
		return nv;
	}

	@Override
	public void onNoteIndenting(Task t, Note n, int new_indent)
	{
		if (!t.isExpanded()) {
			return;
		}

		updateNoteIndent(n, getNoteView(t, n), new_indent);
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
		int dx;
		int dy;

		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			down_x = X;
			down_y = Y;
			dragged = false;
			break;
		case MotionEvent.ACTION_UP:
			dx = X - down_x;
			dy = Y - down_y;
			if (dragged) {
				break;
			}
			Task t = new Task(
					X + TASK_CREATION_OFFSET_X - plan.getViewOffsetX(),
					Y + TASK_CREATION_OFFSET_Y - plan.getViewOffsetY()
			);
			plan.addTask(t);
			editTask(t);
			break;
		case MotionEvent.ACTION_MOVE:
			dx = X - down_x;
			dy = Y - down_y;
			if (!dragged) {
				if (Math.abs(dx) + Math.abs(dy) > TASK_CREATION_THRESHOLD) {
					dragged = true;
					prev_x = down_x;
					prev_y = down_y;
				} else {
					break;
				}
			}
			dx = X - prev_x;
			dy = Y - prev_y;

			drag(dx, dy);

			prev_x = X;
			prev_y = Y;
			break;
		}
		return true;
	}

	private void drag(int dx, int dy)
	{
		plan.setViewOffset(plan.getViewOffsetX() + dx,
				plan.getViewOffsetY() + dy);
	}

	public void onViewDragged(Plan p)
	{
		if (p != plan) {
			return;
		}
		for (View v : this.task2view.values()) {
			Task t = this.task2view.getKey(v);

			updateTask(v, t);
		}
	}

	@Override
	public void onTaskAdded(Plan p, Task t)
	{
		View v = initTaskView(t);
		updateTaskNotes(v, t);
		updateTask(v, t);
	}

	@Override
	public void onNoteAdded(Task t, int index, Note n)
	{
		if (!t.isExpanded()) {
			return;
		}

		ViewGroup vg = (ViewGroup) task2view.get(t);
		BiMap<Note, View> noteviews = taskview2noteviews.get(vg);

		View nv = initNote(n);

		vg.addView(nv, index + 1 /* shift because of title */);
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
		invalidate();
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

	class TaskViewerSurfaceUpdater implements Runnable {
		private TaskViewer tv;

		TaskViewerSurfaceUpdater(TaskViewer tv)
		{
			this.tv = tv;
		}

		@Override
		public void run()
		{
			if ((CF.DEBUG_FLAGS & CF.DEBUG_LOG_TASK_VIEW_INVALIDATES) != 0)
				Log.v("TaskViewerSurfaceUpdater", "drawing");
			tv.tryDrawing(tv.holder);
		}

	}

	public void invalidate()
	{
		if (invalid) {
			return;
		}
		invalid = true;
		handler.postDelayed(updater, 10);
	}
}
