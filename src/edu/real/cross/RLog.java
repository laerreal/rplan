package edu.real.cross;

import android.util.Log;

public class RLog
{

	static public int v(String tag, String msg)
	{
		return Log.v(tag, msg);
	}

	static public int v(String tag, String format, Object... args)
	{
		String msg = String.format(format, args);
		return v(tag, msg);
	}

	static public int v(Class<?> cls, String format, Object... args)
	{
		return v(cls.getPackage().getName() + "." + cls.getName(), format,
				args);
	}
}
