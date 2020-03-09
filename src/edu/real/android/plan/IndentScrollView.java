package edu.real.android.plan;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.ScrollView;
import edu.real.external.CF;

public class IndentScrollView extends ScrollView
		implements OnGestureListener
{
	/* Indentation by fling. */
	GestureDetector gd;
	/* XXX: See comment near usage of this field in TaskEditActivity. */
	TaskEditActivity tea;
	public boolean drag_mode;

	public IndentScrollView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	public IndentScrollView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public IndentScrollView(Context context)
	{
		super(context);
		init();
	}

	private void init()
	{
		drag_mode = false;
		gd = new GestureDetector(this.getContext(), this);
		tea = null;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		if (drag_mode) {
			return false;
		}
		if (tea != null) {
			if (CF.DEBUG < -1)
				Log.i(getClass().toString(), "onInterceptTouchEvent");

			gd.onTouchEvent(ev);
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if (tea != null) {
			if (CF.DEBUG < -1)
				Log.i(getClass().toString(), "onTouchEvent");

			gd.onTouchEvent(ev);
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public boolean onFling(
			MotionEvent e1, MotionEvent e2, float velocityX, float velocityY
	)
	{
		if (CF.DEBUG < 1)
			Log.i(getClass().toString(),
					"onFling " + velocityX + ", " + velocityY);

		if (Math.abs(velocityX) < 1.5 * Math.abs(velocityY)) {
			// fling is not horizontal enough
			return true;
		}

		if (velocityX < 0) {
			tea.indent(-1);
		} else {
			tea.indent(+1);
		}

		return true;
	}

	@Override
	public boolean onDown(MotionEvent e)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(
			MotionEvent e1, MotionEvent e2, float distanceX, float distanceY
	)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{
		// TODO Auto-generated method stub

	}
}
