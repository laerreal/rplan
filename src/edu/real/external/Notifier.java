package edu.real.external;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

public abstract class Notifier<L>
{
	L listeners[];
	LinkedList<L> added_listeners;

	@SuppressWarnings("unchecked")
	public Notifier()
	{
		added_listeners = new LinkedList<L>();
		listeners = (L[]) new Object[1];
	}

	public void addListener(L l)
	{
		added_listeners.addLast(l);
	}

	public boolean removeListener(L l)
	{
		int I = listeners.length;
		for (int i = 0; i < I; i++) {
			if (listeners[i] == l) {
				listeners[i] = null;
				return true;
			}
		}
		return added_listeners.remove(l);
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

