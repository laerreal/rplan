package edu.real.android.plan;

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import edu.real.plan.Subtask;

public class SubtaskCheckedListener implements OnCheckedChangeListener
{
	Subtask st;

	public SubtaskCheckedListener(Subtask subtask)
	{
		st = subtask;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (st.getChecked() == isChecked) {
			return;
		}
		st.setChecked(isChecked);
	}

}
