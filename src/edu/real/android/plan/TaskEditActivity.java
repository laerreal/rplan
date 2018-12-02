package edu.real.android.plan;

import java.util.Iterator;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ToggleButton;
import edu.real.external.BiMap;
import edu.real.external.CF;
import edu.real.plan.Note;
import edu.real.plan.Subtask;
import edu.real.plan.Task;
import edu.real.plan.TextNote;

public class TaskEditActivity extends RPlanActivity implements
		OnClickListener, // for buttons
		OnCheckedChangeListener, // for check box of subtask
		// for notes, to implement indentation of fling
		OnGestureListener, OnTouchListener
{
	public static final int INDENTATION_STEP = 50;
	protected final int MODE_NOT_SET = 0;
	protected final int MODE_SIMPLE = 1;
	protected final int MODE_MANAGE = 2;

	Task task;
	LinearLayout ll_notes;
	int next_note_index;
	EditText et_task_description;
	EditText et_task_name;
	BiMap<Note, View> note2view;
	Button bt_add_note;
	Button bt_add_subtask;
	LayoutInflater inflater;
	LayoutParams note_content_lp;
	int mode;
	ToggleButton tb_edit_mode;
	/* Indentation by fling. */
	GestureDetector gd;
	View last_touched;
	/* Performs time spread user interface operations */
	Handler ui_handler;
	String i_action;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		if (CF.DEBUG < 1)
			Log.v(getClass().getName(), "onCreate");

		super.onCreate(savedInstanceState);

		i_action = getIntent().getAction();
		if (i_action == null) {
			i_action = "";
		}

		ui_handler = new Handler();

		getSupportActionBar().hide();

		setContentView(R.layout.activity_task_edit);

		ll_notes = (LinearLayout) findViewById(R.id.ll_task_notes);
		et_task_description = (EditText) findViewById(R.id.et_task_description);
		et_task_name = (EditText) findViewById(R.id.et_task_name);

		note2view = new BiMap<Note, View>();
		next_note_index = 0;

		bt_add_note = (Button) findViewById(R.id.bt_add_note);
		bt_add_note.setOnClickListener(this);
		bt_add_subtask = (Button) findViewById(R.id.bt_add_subtask);
		bt_add_subtask.setOnClickListener(this);

		inflater = LayoutInflater.from(this);
		note_content_lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, 1);

		mode = MODE_NOT_SET;
		setMode(MODE_SIMPLE);
		tb_edit_mode = (ToggleButton) findViewById(R.id.tb_edit_mode);
		tb_edit_mode.setOnCheckedChangeListener(this);

		gd = new GestureDetector(this, this);

		if (CF.DEBUG < 1)
			Log.v(getClass().getName(), "onCreate-d");
	}


	private void setMode(int m)
	{
		if (m == mode) {
			return;
		}
		mode = m;
		for (View v : note2view.values()) {
			applyModeToNoteView((ViewGroup) v);
		}
	}

	class Initializer implements Runnable
	{
		Iterator<Note> cur;

		Initializer(Iterator<Note> cur)
		{
			this.cur = cur;
		}

		public void run()
		{
			if (cur.hasNext()) {
				Note n = cur.next();
				addViewForNote(n);
				ui_handler.post(this);
				return;
			}

			if (CF.DEBUG < 1)
				Log.v(getClass().getName(), "initialized");
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		super.onServiceConnected(name, service);

		if (task != null) {
			return;
		}

		task = this.service.getPlan().getCurrentTask();

		if (task == null) {
			if (i_action.equals(Intent.ACTION_MAIN)) {
				/* No current task for just started application. Redirect to
				 * main activity. */
				startActivity(new Intent(this, MainActivity.class));
			}
			finish();
			return;
		}

		if (CF.DEBUG < 1)
			Log.v(getClass().getName(), "initializing");

		et_task_name.setText(task.getName());
		et_task_description.setText(task.getDescription());

		ui_handler.post(new Initializer(task.getNotes().iterator()));
	}

	@SuppressLint("InflateParams")
	private View addViewForNote(Note n)
	{
		View main_input = null;

		LinearLayout ll = (LinearLayout) inflater
				.inflate(R.layout.note_edit_container, null);

		/* Delete button */
		View bt_delete = ll.findViewById(R.id.bt_delete_note);
		bt_delete.setOnClickListener(this);
		bt_delete.setTag(n);

		/* Up/Down buttons */
		View bt_move = ll.findViewById(R.id.bt_note_up);
		bt_move.setOnClickListener(this);
		bt_move.setTag(n);

		bt_move = ll.findViewById(R.id.bt_note_down);
		bt_move.setOnClickListener(this);
		bt_move.setTag(n);

		ll_notes.addView(ll, next_note_index++);

		updateIndent(n, ll);

		if (n instanceof Subtask) {
			Subtask st = (Subtask) n;

			CheckBox cb = new CheckBox(this);
			cb.setChecked(st.getChecked());
			cb.setTag(TaskViewer.TAG_CHECKBOX);
			ll.addView(cb, 0);

			EditText et = new EditText(this);
			et.setSingleLine(true);
			et.setText(st.getText());
			et.setTag(TaskViewer.TAG_NAME);

			et.setGravity(Gravity.FILL_HORIZONTAL);
			ll.addView(et, 1, note_content_lp);

			main_input = et;
		} else if (n instanceof TextNote) {
			TextNote tn = (TextNote) n;

			EditText et = new EditText(this);
			et.setSingleLine(true);
			et.setText(tn.getText());
			et.setTag(TaskViewer.TAG_NAME);

			et.setGravity(Gravity.FILL_HORIZONTAL);
			ll.addView(et, 0, note_content_lp);

			main_input = et;
		}

		main_input.setOnTouchListener(this);

		applyModeToNoteView(ll);
		note2view.put(n, ll);

		return main_input;
	}

	private void updateIndent(Note n, View v)
	{
		int indent = n.getIndent();
		LayoutParams lp = (LayoutParams) v.getLayoutParams();
		lp.setMargins(INDENTATION_STEP * indent, 0, 0, 0);
		v.setLayoutParams(lp);
	}

	private void applyModeToNoteView(ViewGroup vg)
	{
		switch (mode) {
		case MODE_SIMPLE:
			vg.findViewById(R.id.bt_note_up).setVisibility(View.GONE);
			vg.findViewById(R.id.bt_note_down).setVisibility(View.GONE);
			vg.findViewById(R.id.bt_delete_note).setVisibility(View.GONE);
			break;
		case MODE_MANAGE:
			vg.findViewById(R.id.bt_note_up).setVisibility(View.VISIBLE);
			vg.findViewById(R.id.bt_note_down).setVisibility(View.VISIBLE);
			vg.findViewById(R.id.bt_delete_note).setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (task == null) {
			return;
		}

		String tmp;
		boolean tmpb;

		tmp = et_task_name.getText().toString();
		if (!tmp.equals(task.getName())) {
			task.setName(tmp);
		}

		tmp = et_task_description.getText().toString();
		if (!tmp.equals(task.getDescription())) {
			task.setDescription(tmp);
		}

		for (Note n : note2view.keys()) {
			if (n instanceof TextNote) {
				TextNote tn = (TextNote) n;
				View v = note2view.get(n);

				if (n instanceof Subtask) {
					CheckBox cb = (CheckBox) v
							.findViewWithTag(TaskViewer.TAG_CHECKBOX);

					tmpb = cb.isChecked();
					Subtask st = (Subtask) n;
					if (st.getChecked() != tmpb) {
						st.setChecked(tmpb);
					}
				}

				EditText et = (EditText) v.findViewWithTag(TaskViewer.TAG_NAME);

				tmp = et.getText().toString();
				if (!tmp.equals(tn.getText())) {
					tn.setText(tmp);
				}
			}
		}
	}

	@Override
	public void onClick(View v)
	{
		Note n;
		if (v == bt_add_note) {
			n = new TextNote();
		} else if (v == bt_add_subtask) {
			n = new Subtask();
		} else {
			Object tag = v.getTag();
			if (tag == null) {
				return;
			}
			n = (Note) tag;
			int idx;
			View nv;
			switch (v.getId()) {
			case R.id.bt_delete_note:
				nv = note2view.pop(n);
				ll_notes.removeView(nv);
				next_note_index--;
				task.removeNote(n);
				break;
			case R.id.bt_note_up:
				nv = note2view.get(n);
				idx = ll_notes.indexOfChild(nv);
				if (idx > 0) {
					ll_notes.removeViewAt(idx);
					idx--;
					ll_notes.addView(nv, idx);
					task.moveNote(n, idx);
				}
				break;
			case R.id.bt_note_down:
				nv = note2view.get(n);
				idx = ll_notes.indexOfChild(nv);
				if (idx < next_note_index - 1) {
					ll_notes.removeViewAt(idx);
					idx++;
					ll_notes.addView(nv, idx);
					task.moveNote(n, idx);
				}
				break;
			}
			return;
		}
		task.addNote(n);
		View input = addViewForNote(n);
		if (input != null) {
			input.requestFocus();
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (buttonView == tb_edit_mode) {
			setMode(isChecked ? MODE_MANAGE : MODE_SIMPLE);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (i_action.equals(Intent.ACTION_MAIN)) {
				/* user explicitly ended work with current task */
				service.getPlan().setCurrentTask(null);
				startActivity(new Intent(this, MainActivity.class));
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}


	/* Gesture detection for notes */

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		last_touched = v;
		gd.onTouchEvent(event);
		// do not mask touch events for note input
		return false;
	}

	@Override
	public boolean onFling(
			MotionEvent e1, MotionEvent e2, float velocityX, float velocityY
	)
	{
		if (CF.DEBUG < 1)
			Log.i(getClass().toString(),
				"onFling " + velocityX + ", " + velocityY);

		if (Math.abs(velocityX) < 1.5 * Math.abs(velocityY)) {
			// fling is not horizontal enough
			return true;
		}

		Object tmp = last_touched;
		Note n = null;
		View nv = null; // init. var., just to make Eclipse checker happy
		while (n == null && tmp instanceof View) {
			nv = (View) tmp;
			n = note2view.getKey(nv);
			tmp = nv.getParent();
		}

		if (n == null) {
			// fling on something that is not a view for a note
			return true;
		}

		final int i = n.getIndent();
		if (i > 0 && velocityX < 0) {
			n.setIndent(i - 1);
		} else if (velocityX > 0) {
			n.setIndent(i + 1);
		}

		updateIndent(n, nv);

		return true;
	}

	@Override
	public boolean onDown(MotionEvent e)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{
		// TODO Auto-generated method stub

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

}
