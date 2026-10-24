package me.eldodebug.soar.management.event;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Copy-on-write registry: (un)registering rebuilds a small array, dispatch only reads it.
 * Registration happens on mod toggle; dispatch happens many times per frame.
 */
public class EventManager {

	private static final Data[] EMPTY = new Data[0];

	private final Map<Class<?>, Data[]> registry = new HashMap<Class<?>, Data[]>();

	public void register(Object o) {
		for (Method method : o.getClass().getDeclaredMethods()) {
			if (isHandler(method)) {
				register(method, o);
			}
		}
	}

	private void register(Method method, Object o) {
		Class<?> clazz = method.getParameterTypes()[0];
		Data[] current = registry.get(clazz);
		if (current == null) {
			current = EMPTY;
		}

		for (Data d : current) {
			if (d.source == o && d.target.equals(method)) {
				return;
			}
		}

		Data data = new Data(o, method, method.getAnnotation(EventTarget.class).value());

		// insert keeping priority order (stable for equal priorities)
		Data[] next = new Data[current.length + 1];
		int i = 0;
		while (i < current.length && current[i].priority <= data.priority) {
			next[i] = current[i];
			i++;
		}
		next[i] = data;
		System.arraycopy(current, i, next, i + 1, current.length - i);
		registry.put(clazz, next);
	}

	public void unregister(Object o) {
		Iterator<Map.Entry<Class<?>, Data[]>> it = registry.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Class<?>, Data[]> entry = it.next();
			Data[] current = entry.getValue();
			int keep = 0;
			for (Data d : current) {
				if (d.source != o) {
					keep++;
				}
			}
			if (keep == current.length) {
				continue;
			}
			if (keep == 0) {
				it.remove();
				continue;
			}
			Data[] next = new Data[keep];
			int j = 0;
			for (Data d : current) {
				if (d.source != o) {
					next[j++] = d;
				}
			}
			entry.setValue(next);
		}
	}

	private static boolean isHandler(Method method) {
		return method.getParameterTypes().length == 1 && method.isAnnotationPresent(EventTarget.class);
	}

	Data[] get(Class<? extends Event> clazz) {
		return registry.get(clazz);
	}
}
