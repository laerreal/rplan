package edu.real.android.plan;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;
import edu.real.external.IOHelper;

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
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, RPlanPreferenceActivity.class));
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
		throw new Exception("Not implemented");
	}
}
