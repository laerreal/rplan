package edu.real.android.plan;

import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;
import edu.real.external.IOHelper;
import edu.real.external.ZonedDateTime;
import edu.real.plan.Plan;
import edu.real.plan.Subtask;
import edu.real.plan.Task;

public class MainActivity extends RPlanActivity
{
	TaskViewer viewer;
	String import_url;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Intent i = getIntent();
		if (i.getAction() == Intent.ACTION_VIEW) {
			import_url = i.getDataString();
		}
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
							service.setPlan(new_plan);
							viewer.update(new_plan);
						}
					})
					.setNegativeButton(R.string.bt_no, null)
					.create()
					.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		super.onServiceConnected(name, service);
		if (viewer == null) {
			viewer = new TaskViewer((RelativeLayout) findViewById(R.id.pane));
		}
		if (import_url != null) {
			try {
				final String import_file;
				if (import_url.startsWith("file:")) {
					import_file = import_url.substring(5);
				} else {
					import_file = import_url;
				}
				String text = IOHelper.getStringFromFile(import_file);
				importMobileNotesJSON(text);
			} catch (Exception e) {
				String msg = e.toString();
				Log.e("MobileNotes", msg);
				Toast.makeText(getApplicationContext(), msg,
						Toast.LENGTH_LONG).show();
			}
			// import file only once
			import_url = null;
		}
		viewer.update(this.service.getPlan());
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
}
