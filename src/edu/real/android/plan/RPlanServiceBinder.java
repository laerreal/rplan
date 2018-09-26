package edu.real.android.plan;

import android.os.Binder;

public class RPlanServiceBinder extends Binder
{
	RPlanService service;

	public RPlanService getService()
	{
		return service;
	}

	public RPlanServiceBinder(RPlanService service)
	{
		this.service = service;
	}

}
