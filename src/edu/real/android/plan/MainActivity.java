package edu.real.android.plan;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

public class MainActivity extends ActionBarActivity implements ServiceConnection
{
	TaskViewer viewer;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		bindService(new Intent(getApplicationContext(), RPlanService.class),
				this, BIND_AUTO_CREATE);
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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		RPlanServiceBinder binder = (RPlanServiceBinder) service;
		viewer = new TaskViewer(binder.getService().getPlan(),
				(RelativeLayout) this.findViewById(R.id.pane));
		viewer.init();
		viewer.update();
	}

	@Override
	public void onServiceDisconnected(ComponentName name)
	{
		// TODO Auto-generated method stub
	}
}
