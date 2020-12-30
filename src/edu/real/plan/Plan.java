package edu.real.plan;

import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import edu.real.external.Notifier;

public class Plan extends Notifier<PlanListener>
{
	List<Task> tasks;
	Task current_task;
	int view_offset_x;
	int view_offset_y;

	/**
	 * Unsupported data read from string during loading. Preserved to be saved.
	 * This is forward compatibility.
	 */
	String opaque;

	public Plan()
	{
		tasks = new LinkedList<Task>();
	}

	public int getViewOffsetX()
	{
		return view_offset_x;
	}

	public int getViewOffsetY()
	{
		return view_offset_y;
	}

	public void setViewOffset(int x, int y)
	{
		view_offset_x = x;
		view_offset_y = y;
		for (begin(); next(); l.onViewDragged(this))
			;
	}

	public void addTask(Task task)
	{
		tasks.add(task);
		for (begin(); next(); l.onTaskAdded(this, task))
			;
	}

	public Collection<Task> getTasks()
	{
		return new LinkedList<Task>(tasks);
	}

	public Task getCurrentTask()
	{
		return current_task;
	}

	public void setCurrentTask(Task t)
	{
		current_task = t;
	}

	public void removeTask(Task task)
	{
		for (begin(); next(); l.onTaskRemoving(this, task))
			;
		tasks.remove(task);
	}

	public int getTaskIndex(Task t)
	{
		return tasks.indexOf(t);
	}

	public void save(StringWriter w)
	{
		if (!tasks.isEmpty()) {
			w.write("tasks\n");
			for (Task t : tasks) {
				t.save(w);
				w.write("\n");
			}
			w.write("\n");
		}
		if (current_task != null) {
			w.write(String.format("current_task %d\n",
					getTaskIndex(current_task)));
		}
		w.write(String.format("view_offset_x %d\n", view_offset_x));
		w.write(String.format("view_offset_y %d\n", view_offset_y));
		if (opaque != null) {
			w.write(opaque);
		}
	}

	public String saveAsString()
	{
		StringWriter w = new StringWriter();
		save(w);
		return w.toString();
	}

	public static final Plan load(String text)
			throws IllegalAccessException, IllegalArgumentException
	{
		String lines[] = text.split("\n");
		return load(lines);
	}

	public static final Plan load(String lines[])
			throws IllegalAccessException, IllegalArgumentException
	{
		Plan ret = new Plan();

		int current_task = -1;
		String opaque = "";
		int i = 0;

		for (; i < lines.length; i++) {
			String l = lines[i];

			final String fieldName;
			final String fieldValue;

			int sep = l.indexOf(" ");
			if (sep < 0) {
				fieldName = l;
				fieldValue = null;
			} else {
				fieldName = l.substring(0, sep);
				fieldValue = l.substring(sep + 1);
			}
			if (fieldName.equals("opaque")) {
				// cannot load "opaque" explicitly
				opaque += l + "\n";
				continue;
			}

			if (fieldName.equals("current_task")) {
				current_task = Integer.parseInt(fieldValue);
			} else if (fieldName.equals("tasks")) {
				List<String> task_lines = new LinkedList<String>();
				Task t;
				for (i++; i < lines.length; i++) {
					l = lines[i];
					if (l.length() == 0) {
						if (task_lines.size() > 0) {
							try {
								t = Task.load(
										task_lines.toArray(
												new String[task_lines.size()]));
								ret.tasks.add(t);
							} catch (Exception e) {
								for (String ll : task_lines) {
									opaque += ll + "\n";
								}
								opaque += "\n";
							} finally {
								task_lines.clear();
							}
						} else {
							break;
						}
					} else {
						task_lines.add(l);
					}
				}
				if (task_lines.size() > 0) {
					try {
						t = Task.load(
								task_lines.toArray(
										new String[task_lines.size()]));
						ret.tasks.add(t);
					} catch (Exception e) {
						for (String ll : task_lines) {
							opaque += ll + "\n";
						}
						opaque += "\n";
					} finally {
						task_lines.clear();
					}
				}
			} else if (fieldName.equals("view_offset_x")) {
				ret.view_offset_x = Integer.parseInt(fieldValue);
			} else if (fieldName.equals("view_offset_y")) {
				ret.view_offset_y = Integer.parseInt(fieldValue);
			} else {
				opaque += l + "\n";
				continue;
			}
		}

		if (current_task >= 0 && ret.tasks.size() > current_task) {
			ret.current_task = ret.tasks.get(current_task);
		}

		if (opaque.equals("")) {
			ret.opaque = null;
		} else {
			ret.opaque = opaque;
		}

		return ret;
	}
}
