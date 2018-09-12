package edu.real.android.plan;

import android.view.SurfaceView;
import edu.real.plan.Plan;

public class TaskViewer
{
	Plan plan;
	SurfaceView surface;

	public TaskViewer(Plan plan, SurfaceView surface)
	{
		this.plan = plan;
		this.surface = surface;
	}
}
