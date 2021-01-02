package edu.real.android.plan;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;


public class RPlanActivity extends AppCompatActivity
		implements ServiceConnection
{

	protected RPlanService service;

	@Override
	protected void onStart()
	{
		super.onStart();

		bindService(new Intent(getApplicationContext(), RPlanService.class),
				this, BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		service = null;
		unbindService(this);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		RPlanServiceBinder binder = (RPlanServiceBinder) service;
		this.service = binder.getService();
	}

	@Override
	public void onServiceDisconnected(ComponentName name)
	{
		service = null;
	}

}
