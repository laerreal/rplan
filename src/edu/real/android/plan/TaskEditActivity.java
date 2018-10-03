package edu.real.android.plan;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import edu.real.external.BiMap;
import edu.real.plan.Note;
import edu.real.plan.Subtask;
import edu.real.plan.Task;
import edu.real.plan.TextNote;

public class TaskEditActivity extends RPlanActivity implements OnClickListener
{
	Task task;
	LinearLayout ll_notes;
	int next_note_index;
	EditText et_task_description;
	EditText et_task_name;
	BiMap<Note, View> note2view;
	Button bt_add_note;
	Button bt_add_subtask;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
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
			this.finish();
		}

		et_task_name.setText(task.getName());
		et_task_description.setText(task.getDescription());

		for (Note n : task.getNotes()) {
			addViewForNote(n);
		}
	}

	private void addViewForNote(Note n)
	{
		if (n instanceof Subtask) {
			Subtask st = (Subtask) n;

			LinearLayout ll = new LinearLayout(this);
			ll.setOrientation(LinearLayout.HORIZONTAL);

			CheckBox cb = new CheckBox(this);
			cb.setChecked(st.getChecked());
			cb.setTag(TaskViewer.TAG_CHECKBOX);
			ll.addView(cb);

			EditText et = new EditText(this);
			et.setSingleLine(true);
			et.setText(st.getText());
			et.setTag(TaskViewer.TAG_NAME);

			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);

			ll.addView(et, lp);

			ll_notes.addView(ll, next_note_index++);

			note2view.put(st, ll);
		} else if (n instanceof TextNote) {
			TextNote tn = (TextNote) n;

			EditText et = new EditText(this);
			et.setSingleLine(true);
			et.setText(tn.getText());
			ll_notes.addView(et, next_note_index++);

			note2view.put(tn, et);
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
				final EditText et;
				if (n instanceof Subtask) {
					View v = note2view.get(n);
					CheckBox cb = (CheckBox) v
							.findViewWithTag(TaskViewer.TAG_CHECKBOX);

					tmpb = cb.isChecked();
					Subtask st = (Subtask) n;
					if (st.getChecked() != tmpb) {
						st.setChecked(tmpb);
					}

					et = (EditText) v
							.findViewWithTag(TaskViewer.TAG_NAME);
				} else {
					et = (EditText) note2view.get(n);
				}

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
			return;
		}
		task.addNote(n);
		addViewForNote(n);
	}
}
