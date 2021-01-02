package edu.real.external;

public class CF
{
	/* compile flags */

	/* Less value - more prints */
	static public final int DEBUG = 0;
	static public final int DEBUG_LOG_TASK_VIEW_INVALIDATES = 1;
	static public final int DEBUG_LOG_TASK_VIEW_LAYOUT_PARAMS = 2;
	static public final int DEBUG_NOTIFIER = 4;

	static public final int DEBUG_FLAGS = 0
		// | DEBUG_LOG_TASK_VIEW_INVALIDATES
		// | DEBUG_LOG_TASK_VIEW_LAYOUT_PARAMS
		| DEBUG_NOTIFIER
		;

	static public boolean isSet(int flags)
	{
		return (DEBUG_FLAGS & flags) != 0;
	}
}
