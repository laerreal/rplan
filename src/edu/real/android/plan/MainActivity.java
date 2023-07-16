package edu.real.android.plan;

import java.net.URLDecoder;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;
import edu.real.cross.RLog;
import edu.real.external.CF;
import edu.real.external.IOHelper;
import edu.real.external.ZonedDateTime;
import edu.real.plan.Plan;
import edu.real.plan.PlanListener;
import edu.real.plan.Subtask;
import edu.real.plan.Task;

public class MainActivity extends RPlanActivity implements PlanListener
{
	public static final String INTENT_ACTION_SELECT_TASK = "edu.real.android.plan.intent.SELECT_TASK";

	TaskViewer viewer;
	String import_url;

	private Plan current_plan;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		Intent i = getIntent();
		if (i.getAction() == Intent.ACTION_VIEW) {
			import_url = i.getDataString();
			// handle this intent only once
			i.setAction(Intent.ACTION_MAIN);
		}
	}

	@Override
	protected void onStop()
	{
		setPlan(null);
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id) {
		case R.id.action_settings:
			startActivity(new Intent(this, RPlanPreferenceActivity.class));
			return true;
		case R.id.remove_all:
			(new AlertDialog.Builder(this))
					.setTitle(R.string.remove_all_tasks_)
					.setPositiveButton(R.string.bt_yes, new OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							Plan new_plan = new Plan();
							setPlan(new_plan);
							service.setPlan(new_plan);
						}
					})
					.setNegativeButton(R.string.bt_no, null)
					.create()
					.show();
			return true;
		case R.id.export_share:
			if (service == null) {
				return true;
			}
			Plan plan = service.getPlan();
			Intent shareBackup = new Intent(Intent.ACTION_SEND);
			shareBackup.putExtra(Intent.EXTRA_TEXT, plan.saveAsString());
			shareBackup.putExtra(Intent.EXTRA_SUBJECT, "RPlan Backup");
			shareBackup.setType("text/plain");

			Intent shareIntent = Intent.createChooser(shareBackup, null);
			startActivity(shareIntent);
			return true;
		case R.id.import_:
			Intent importIntent = new Intent(this, ImportActivity.class);
			startActivity(importIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder binder)
	{
		super.onServiceConnected(name, binder);
		if (viewer == null) {
			viewer = new TaskViewer((RelativeLayout) findViewById(R.id.pane));
		}
		if (import_url != null) {
			String url = import_url;
			// import file only once
			import_url = null;
			try {
				final String import_file;
				if (url.startsWith("file://")) {
					url = url.substring(7);

					import_file = URLDecoder.decode(url, "utf-8");
					String text = IOHelper.getStringFromFile(import_file);
					try {
						importMobileNotesJSON(text);
					} catch (JSONException json_e) {
						Intent import_text_intent = new Intent(
							this,
							ImportActivity.class
						);
						import_text_intent.setAction(
							ImportActivity.INTENT_ACTION_START_WITH_TEXT
						);
						import_text_intent.putExtra(Intent.EXTRA_TEXT, text);
						startActivity(import_text_intent);
					}
				} else {
					Toast.makeText(getApplicationContext(),
						String.format(
							"URL '%s' is not supported!",
							url
						),
						Toast.LENGTH_LONG
					).show();
				}
			} catch (Exception e) {
				String msg = e.toString();
				RLog.e(getClass(), msg);
				Toast.makeText(getApplicationContext(), msg,
						Toast.LENGTH_LONG).show();
			}
		}
		setPlan(service.getPlan());
	}

	@Override
	public void onServiceDisconnected(ComponentName name)
	{
		/* The service has crashed.
		 * See: https://stackoverflow.com/questions/971824/when-does-serviceconnection-onservicedisconnected-get-called */
		/* TODO: should we finish or re-bind to the service. */
		setPlan(null);
		super.onServiceDisconnected(name);
	}

	protected void setPlan(Plan plan)
	{
		if (current_plan != null) {
			current_plan.removeListener(this);
		}
		current_plan = plan;
		viewer.update(plan);
		if (plan != null) {
			plan.addListener(this);
			handleCurrentTask(plan.getCurrentTask());
		}
	}

	protected void importMobileNotesJSON(String json_text) throws Exception
	{
		Plan plan = service.getPlan();

		JSONObject root = new JSONObject(json_text);
		JSONArray lists = root.getJSONArray("lists");
		TimeZone mobile_notes_tz = TimeZone.getTimeZone("GMT");
		for (int i = 0, I = lists.length(); i < I; i++) {
			JSONObject list = lists.getJSONObject(i);
			JSONArray rows = list.getJSONArray("rows");
			JSONObject row;
			row = rows.getJSONObject(0);
			String task_name = row.getString("text");

			Task t = new Task();
			t.setName(task_name);
			t.setCreationTS(new ZonedDateTime(list.getLong("created"),
					mobile_notes_tz));

			for (int j = 1, J = rows.length(); j < J; j++) {
				row = rows.getJSONObject(j);
				Subtask s = new Subtask(row.getString("text"),
						row.getInt("checked") != 0);

				t.addNote(s);
			}

			// after all editing, of course
			t.setLastEditedTS(new ZonedDateTime(list.getLong("last_edited"),
					mobile_notes_tz));

			plan.addTask(t);
		}
	}

	/* Most of those events are handled by helper class TaskViewer. */
	@Override
	public void onTaskAdded(Plan p, Task t)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onTaskRemoving(Plan p, Task t)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onViewDragged(Plan p)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onCurrentTaskSet(Plan o, Task t)
	{
		handleCurrentTask(t);
	}

	private void startTaskEditting()
	{
		if (CF.isSet(CF.DEBUG_ACTIVITY_WORKFLOW))
			RLog.v(getClass(), "starting task editing");

		Intent intent = new Intent(this, TaskEditActivity.class);
		intent.setAction(TaskEditActivity.INTENT_ACTION_EDIT_TASK);
		startActivity(intent);

		if (CF.isSet(CF.DEBUG_ACTIVITY_WORKFLOW))
			RLog.v(getClass(), "task editing started");
	}

	private void handleCurrentTask(Task t)
	{
		if (t != null) {
			Intent intent = getIntent();
			if (intent.getAction().equals(INTENT_ACTION_SELECT_TASK)) {
				/* This activity done what it has been asked for: task
				 * selection. */
				finish();
			} else {
				startTaskEditting();
			}
		}
	}
}
