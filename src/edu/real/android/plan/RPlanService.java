package edu.real.android.plan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import edu.real.cross.RLog;
import edu.real.external.CF;
import edu.real.plan.Plan;
import edu.real.plan.Subtask;
import edu.real.plan.Task;
import edu.real.plan.TextNote;

public class RPlanService extends Service
{
	File internal_storage;
	File tmp_plan_file, plan_file;
	Plan plan;

	@Override
	public void onCreate()
	{
		if (CF.isSet(CF.DEBUG_ACTIVITY_WORKFLOW))
			RLog.v(getClass(), "onCreate");

		internal_storage = getFilesDir();
		tmp_plan_file = new File(internal_storage, "plan.tmp");
		plan_file = new File(internal_storage, "plan.txt");

		if (plan_file.exists() && plan_file.length() > 0) {
			try {
				FileInputStream s = new FileInputStream(plan_file);
				byte[] bytes = new byte[(int) plan_file.length()];
				s.read(bytes);
				try {
					String full_s = new String(bytes, "UTF-8");
					try {
						Plan p = Plan.load(full_s);
						plan = p;
						return;
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					s.close();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Plan p = new Plan();
		Task t = new Task();
		t.setName("Test task #1");
		t.move(10, 10);
		t.addNote(new TextNote("This is first example note"));
		t.addNote(new TextNote("This is 2nd example note"));
		t.addNote(new TextNote("This is last example note"));
		t.addNote(new Subtask("A subtask"));
		t.addNote(new Subtask("Another subtask", true));
		t.addNote(new TextNote("text with 1 indent", 1));
		t.addNote(new Subtask("subtask with 2 indent", false, 2));
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

	public void onDestroy()
	{
		if (CF.isSet(CF.DEBUG_ACTIVITY_WORKFLOW))
			RLog.v(getClass(), "onDestroy");

		String plan_s = plan.saveAsString();
		byte[] bytes;
		try {
			bytes = plan_s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return;
		}
		try {
			FileOutputStream s = new FileOutputStream(tmp_plan_file);
			s.write(bytes);
			s.close();
			plan_file.delete();
			tmp_plan_file.renameTo(plan_file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public void setPlan(Plan plan)
	{
		this.plan = plan;
	}
}
