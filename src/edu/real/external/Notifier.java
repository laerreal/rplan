package edu.real.external;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

import edu.real.cross.RLog;

public abstract class Notifier<L>
{
	L listeners[];
	LinkedList<L> added_listeners;

	int empty;
	int leftmost_hole;
	// Holes between elements in `listeners`
	boolean holes;

	// iteration mode: begin(), next(), l.
	int i, I;
	protected L l;

	@SuppressWarnings("unchecked")
	public Notifier()
	{
		added_listeners = new LinkedList<L>();
		empty = 1;
		leftmost_hole = 0;
		holes = false;
		listeners = (L[]) new Object[empty];
	}

	public void addListener(L l)
	{
		added_listeners.addLast(l);

		if (CF.isSet(CF.DEBUG_NOTIFIER))
			RLog.v(this.getClass(), "Added. Total listeners: %d",
					added_listeners.size() + listeners.length);
	}

	public boolean removeListener(L l)
	{
		Boolean ret = null;

		int I = listeners.length;
		for (int i = 0; i < I; i++) {
			if (listeners[i] == l) {
				listeners[i] = null;
				empty += 1;
				if (!holes) {
					if (i < leftmost_hole - 1) {
						holes = true;
					}
				}
				if (i < leftmost_hole) {
					leftmost_hole = i;
				}

				ret = true;
				break;
			}
		}
		if (ret == null) {
			ret = added_listeners.remove(l);
		}

		if (CF.isSet(CF.DEBUG_NOTIFIER))
			RLog.v(this.getClass(), "Removed. Total listeners: %d",
					added_listeners.size() + listeners.length);

		return ret;
	}

	/**
	 * use example:
	 *
	 * for(begin(); next(); l.onEvent(arg1, arg2, ...));
	 */

	@SuppressWarnings("unchecked")
	protected void begin()
	{
		// prepare listeners array
		final int added = added_listeners.size();

		int i, j; // local i, j
		final int I = listeners.length; // local I
		// copy array if current size is insufficient
		if (added > empty) {
			final Object[] nl = new Object[I + added - empty];
			// holes are removed during copying
			for (i = 0, j = 0; j < I; i++, j++) {
				L l;
				do {
					l = listeners[j];
					if (l != null) {
						break;
					}
					j++;
				} while (j < I);
				if (l == null) {
					break;
				}
				nl[i] = l;
			}
			listeners = (L[]) nl;
			// all slots will be filled during the loop below
			holes = false;
			empty = 0;
		} else if (holes) {
			// remove holes
			// 1. skip filled slots until first hole
			i = leftmost_hole;
			j = i + 1;
			// 2. remove holes by left shifting
			// leftmost hole index is also computed
			for (; j < I; i = leftmost_hole, j++, i++) {
				L l;
				do {
					l = listeners[j];
					if (l != null) {
						break;
					}
					j++;
				} while (j < I);
				if (l == null) {
					break;
				}
				listeners[i] = l;
			}
			// some slots will be filled during the loop below
			empty -= added;
			holes = false;
		} else {
			i = leftmost_hole;
			empty -= added;
		}

		leftmost_hole = i + added;
		for (; !added_listeners.isEmpty(); i++) {
			listeners[i] = added_listeners.removeFirst();
		}

		// global i, I
		this.i = 0;
		this.I = listeners.length;
	}

	protected boolean next()
	{
		// skipping holes
		while (i < I) {
			l = listeners[i];
			i++;
			if (l != null) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	protected void notify(Method m, Object... args)
	{
		int i = 0, I = listeners.length, j = 0;
		for (; i < I; i++) {
			L l = listeners[i];
			if (l == null) {
				if (j < i) {
					j = i;
				}
				do {
					j++;
					if (j == I) {
						break;
					}
					l = listeners[j];
				} while (l == null);

				if (l == null) {
					break;
				}
				listeners[i] = l;
				listeners[j] = null;
			}
			try {
				m.invoke(l, args);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		final int S = added_listeners.size();
		if (S == 0) {
			return;
		}
		// copy array if current size is insufficient
		if (S < I - i) {
			Object[] nl = new Object[i + S];
			for (j = 0; j < i; j++) {
				nl[j] = listeners[j];
			}
			listeners = (L[]) nl;
		}

		for (; !added_listeners.isEmpty() && i < I; i++) {
			listeners[i] = added_listeners.removeFirst();
		}
	}
}
