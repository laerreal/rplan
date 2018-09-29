package edu.real.android.plan;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class TaskEditText extends EditText
{
	public TaskEditText(Context context)
	{
		super(context);
		init();
	}

	public TaskEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public TaskEditText(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	protected void init()
	{
		mode = MODE_NONE;
		setMode(MODE_READ);
	}

	protected static final int MODE_NONE = 0;
	public static final int MODE_READ = 1;
	public static final int MODE_WRITE = 2;
	protected int mode;

	public void setMode(int m) {
		if (m == mode) {
			return;
		}
		switch (m) {
		case MODE_READ:
			setFocusable(false);
			break;
		case MODE_WRITE:
			setFocusable(true);
			break;
		}
		mode = m;
	}

}
