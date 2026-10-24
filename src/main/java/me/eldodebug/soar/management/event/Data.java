package me.eldodebug.soar.management.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public final class Data {

	private static final MethodType HANDLER_TYPE = MethodType.methodType(void.class, Object.class, Event.class);

	public final Object source;
	public final Method target;
	public final byte priority;
	final MethodHandle handle;

	public Data(Object source, Method target, byte priority) {
		this.source = source;
		this.target = target;
		this.priority = priority;
		try {
			target.setAccessible(true);
			// Typed handle: invokeExact avoids the Object[] varargs array Method.invoke allocates per call
			this.handle = MethodHandles.lookup().unreflect(target).asType(HANDLER_TYPE);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Cannot access event handler " + target, e);
		}
	}
}
