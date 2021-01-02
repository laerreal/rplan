package edu.real.external;

public class CF
{
	/* compile flags */

	static private int bit = 0;

	static public final int DEBUG_LOG_TASK_VIEW_INVALIDATES = 1 << (bit++);
	static public final int DEBUG_LOG_TASK_VIEW_LAYOUT_PARAMS = 1 << (bit++);
	static public final int DEBUG_NOTIFIER = 1 << (bit++);
	static public final int DEBUG_INDENT_SCROLL_VIEW = 1 << (bit++);
	static public final int DEBUG_ACTIVITY_WORKFLOW = 1 << (bit++);

	static protected final int DEBUG_FLAGS = 0
		// | DEBUG_LOG_TASK_VIEW_INVALIDATES
		// | DEBUG_LOG_TASK_VIEW_LAYOUT_PARAMS
		// | DEBUG_NOTIFIER
		// | DEBUG_INDENT_SCROLL_VIEW
		// | DEBUG_ACTIVITY_WORKFLOW
		;

	static public boolean isSet(int flags)
	{
		return (DEBUG_FLAGS & flags) != 0;
	}
}
