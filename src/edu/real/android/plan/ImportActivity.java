package edu.real.android.plan;

import java.text.ParseException;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.real.cross.RLog;
import edu.real.plan.Plan;
import edu.real.plan.Task;

public class ImportActivity extends RPlanActivity implements OnClickListener
{

	EditText et;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_activity);

		((Button) findViewById(R.id.bt_import)).setOnClickListener(this);

		et = (EditText) findViewById(R.id.et_import_text);
		/*
		et.setText(R.string.insert_imported_here_);
		*/
		Toast.makeText(getApplicationContext(),
				R.string.insert_imported_here_,
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onServiceConnected(ComponentName _name, IBinder _service)
	{
		super.onServiceConnected(_name, _service);

		Plan plan = service.getPlan();
		int x = -plan.getViewOffsetX();
		int y = -plan.getViewOffsetY();
		String coords = String.format(" (%d; %d)", x,  y);
		((TextView) findViewById(R.id.tv_xy)).setText(coords);
	}

	@Override
	public void onClick(View v)
	{
		/* Import */

		if (service == null) {
			/* This is very unlikely. */
			Toast.makeText(getApplicationContext(),
					"Please wait for service connection",
					Toast.LENGTH_LONG).show();
			return;
		}

		String text = et.getText().toString();

		try {
			if (text.startsWith(Task.TASK_PREFIX)) {
				importTask(text);
			} else {
				importPlan(text);
			}
		} catch (Exception e) {
			String msg = e.toString();
			RLog.e(getClass(), msg);
			Toast.makeText(getApplicationContext(), msg,
					Toast.LENGTH_LONG).show();
			return;
		}
	}

	private void importTask(String task_s) throws IllegalAccessException,
			IllegalArgumentException, ParseException
	{
		final Task task = Task.load(task_s);

		Plan plan = service.getPlan();

		plan.addTask(task);

		CheckBox cb = (CheckBox) findViewById(R.id.cb_move_to_current_place);
		if (cb.isChecked()) {
			task.move(-plan.getViewOffsetX(), -plan.getViewOffsetY());
		} else {
			plan.setViewOffset(-task.getX(), -task.getY());
		}

		finish();
	}

	private void importPlan(String plan_s)
			throws IllegalAccessException, IllegalArgumentException
	{
		final Plan imported = Plan.load(plan_s);

		(new AlertDialog.Builder(this))
			.setTitle(R.string.imported_replaces_current_)
			.setPositiveButton(R.string.bt_yes,
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which)
					{
						service.setPlan(imported);
						finish();
					}
				}
			)
			.setNegativeButton(R.string.bt_no, null)
			.create()
			.show();
	}
}
