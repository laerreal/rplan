package edu.real.android.plan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import edu.real.cross.RLog;
import edu.real.plan.Plan;

public class ImportActivity extends RPlanActivity implements OnClickListener {

	EditText et;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_activity);

		((Button)findViewById(R.id.bt_import)).setOnClickListener(this);

		et = (EditText)findViewById(R.id.et_import_text);
		/*
		et.setText(R.string.insert_imported_here_);
		*/
		Toast.makeText(getApplicationContext(),
				R.string.insert_imported_here_,
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onClick(View v) {
		/* Import */

		String plan_s = et.getText().toString();
		final Plan imported;
		try {
			imported = Plan.load(plan_s);
		} catch (Exception e) {
			String msg = e.toString();
			RLog.e(getClass(), msg);
			Toast.makeText(getApplicationContext(), msg,
					Toast.LENGTH_LONG).show();
			return;
		}

		if (service == null) {
			/* This is very unlikely. */
			Toast.makeText(getApplicationContext(),
					"Please wait for service connection",
					Toast.LENGTH_LONG).show();
			return;
		}

		(new AlertDialog.Builder(this))
			.setTitle(R.string.imported_replaces_current_)
			.setPositiveButton(R.string.bt_yes,
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which)
					{
						service.setPlan(imported);
						finish();
					}
				}
			)
			.setNegativeButton(R.string.bt_no, null)
			.create()
			.show();
	}
}
