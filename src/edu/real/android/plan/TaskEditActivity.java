package edu.real.android.plan;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.EditText;
import android.widget.LinearLayout;
import edu.real.plan.Task;

public class TaskEditActivity extends RPlanActivity
{
	Task task;
	LinearLayout ll_notes;
	EditText et_task_description;
	EditText et_task_name;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_edit);

		ll_notes = (LinearLayout) findViewById(R.id.ll_task_notes);
		et_task_description = (EditText) findViewById(R.id.et_task_description);
		et_task_name = (EditText) findViewById(R.id.et_task_name);
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
	}
}
