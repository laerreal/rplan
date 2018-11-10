package edu.real.android.plan;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class RPlanPreferenceActivity extends PreferenceActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
