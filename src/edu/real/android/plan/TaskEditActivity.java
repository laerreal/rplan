package edu.real.android.plan;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import edu.real.external.BiMap;
import edu.real.plan.Note;
import edu.real.plan.Task;
import edu.real.plan.TextNote;

public class TaskEditActivity extends RPlanActivity
{
	Task task;
	LinearLayout ll_notes;
	EditText et_task_description;
	EditText et_task_name;
	BiMap<Note, View> note2view;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_edit);

		ll_notes = (LinearLayout) findViewById(R.id.ll_task_notes);
		et_task_description = (EditText) findViewById(R.id.et_task_description);
		et_task_name = (EditText) findViewById(R.id.et_task_name);

		note2view = new BiMap<Note, View>();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		super.onServiceConnected(name, service);
		task = this.service.getPlan().getCurrentTask();

		if (task == null) {
			this.finish();
		}

		et_task_name.setText(task.getName());
		et_task_description.setText(task.getDescription());

		for (Note n : task.getNotes()) {
			if (n instanceof TextNote) {
				TextNote tn = (TextNote) n;

				EditText et = new EditText(this);
				et.setSingleLine(true);
				et.setText(tn.getText());
				ll_notes.addView(et);

				note2view.put(tn, et);
			}
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

		tmp = et_task_name.getText().toString();
		if (tmp != task.getName()) {
			task.setName(tmp);
		}

		tmp = et_task_description.getText().toString();
		if (tmp != task.getDescription()) {
			task.setDescription(tmp);
		}

		for (Note n : note2view.keys()) {
			if (n instanceof TextNote) {
				EditText et = (EditText) note2view.get(n);
				TextNote tn = (TextNote) n;
				tmp = et.getText().toString();
				if (tmp != tn.getText()) {
					tn.setText(tmp);
				}
			}
		}
	}
}
