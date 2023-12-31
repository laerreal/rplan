package edu.real.android.plan;

import android.graphics.Rect;
import android.view.View;
import edu.real.cross.RLog;
import edu.real.external.CF;
import edu.real.plan.Task;
import android.widget.RelativeLayout.LayoutParams;

public class TaskViewTag {

	private int measure_w = 1000;
	private int measure_h = 1000;
	private int measured_w;
	private int measured_h;
	private View v;
	private Task t;
	private LayoutParams lp;
	/* TODO: now we do not detect task invalidation */
	private final boolean measured = false;
	private final int frame_padding = 5;

	public TaskViewTag(View v, Task t)
	{
		this.v = v;
		this.t = t;
	}

	private void measure()
	{
		if (measured) {
			return;
		}

		v.measure(measure_w, measure_h);
		measured_w = v.getMeasuredWidth();
		measured_h = v.getMeasuredHeight();

		if (CF.isSet(CF.DEBUG_LOG_TASK_VIEW_LAYOUT_PARAMS))
			RLog.v(getClass(), "Measured: %d %d", measured_w, measured_h);

		lp = new LayoutParams(measured_w, measured_h);
	}

	public LayoutParams getLayoutParams(int offset_x, int offset_y)
	{
		measure();

		int x = t.getX(), y = t.getY();

		LayoutParams lp = new LayoutParams(measured_w, measured_h);
		lp.leftMargin = x + offset_x;
		lp.topMargin = y + offset_y;

		View p = (View) v.getParent();
		int pw = p.getWidth();
		int ph = p.getHeight();

		int right = (x + offset_x + measured_w + 1);
		int bottom = (y + offset_y + measured_h + 1);

		// Add 1 extra pixel for sureness.
		lp.rightMargin = pw - right;
		lp.bottomMargin = ph - bottom;

		if (CF.isSet(CF.DEBUG_LOG_TASK_VIEW_LAYOUT_PARAMS))
			RLog.v(getClass(), "LP: %d %d %d %d", lp.leftMargin, lp.topMargin,
					lp.rightMargin, lp.bottomMargin);

		return lp;
	}

	public void updateLayoutParams(int offset_x, int offset_y)
	{
		v.setLayoutParams(getLayoutParams(offset_x, offset_y));
	}

	public Rect getFrameRect(int offset_x, int offset_y)
	{
		measure();
		int x = t.getX() + offset_x;
		int y = t.getY() + offset_y;
		return new Rect(
			x - frame_padding, y - frame_padding,
			x + measured_w + frame_padding, y + measured_h + frame_padding
		);
	}
}
