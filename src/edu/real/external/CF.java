package edu.real.external;

public class CF
{
	/* compile flags */

	/* Less value - more prints */
	static public final int DEBUG = 0;
	static public final int DEBUG_LOG_TASK_VIEW_INVALIDATES = 1;

	static public final int DEBUG_FLAGS = 0
		// | DEBUG_LOG_TASK_VIEW_INVALIDATES
		;

	static public boolean isSet(int flags)
	{
		return (DEBUG_FLAGS & DEBUG_LOG_TASK_VIEW_LAYOUT_PARAMS) != 0;
	}
}
