package edu.real.android.plan;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import edu.real.plan.Plan;
import edu.real.plan.Task;
import edu.real.plan.TextNote;

public class MainActivity extends ActionBarActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Plan p = new Plan();
		Task t = new Task();
		t.setName("Test task #1");
		t.move(10, 10);
		t.addNote(new TextNote("This is first example note"));
		t.addNote(new TextNote("This is 2nd example note"));
		t.addNote(new TextNote("This is last example note"));
		p.addTask(t);
		TaskViewer viewer = new TaskViewer(p,
				(RelativeLayout) this.findViewById(R.id.pane));
		viewer.init();
		viewer.update();
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
}
