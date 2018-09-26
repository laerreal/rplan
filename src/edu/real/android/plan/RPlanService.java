package edu.real.android.plan;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import edu.real.plan.Plan;
import edu.real.plan.Task;
import edu.real.plan.TextNote;

public class RPlanService extends Service
{
	Plan plan;

	public RPlanService()
	{
		Plan p = new Plan();
		Task t = new Task();
		t.setName("Test task #1");
		t.move(10, 10);
		t.addNote(new TextNote("This is first example note"));
		t.addNote(new TextNote("This is 2nd example note"));
		t.addNote(new TextNote("This is last example note"));
		p.addTask(t);

		plan = p;
	}

	public Plan getPlan()
	{
		return plan;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return new RPlanServiceBinder(this);
	}
}
