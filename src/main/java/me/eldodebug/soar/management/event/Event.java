package me.eldodebug.soar.management.event;

import me.eldodebug.soar.Glide;

public abstract class Event {

	private boolean cancelled;

	public Event call() {
		this.cancelled = false;
		Event.call(this);
		return this;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	private static void call(Event event) {
		Data[] handlers = Glide.getInstance().getEventManager().get(event.getClass());

		if (handlers == null) {
			return;
		}

		// snapshot array: handlers that (un)register during dispatch don't disturb this loop
		for (int i = 0; i < handlers.length; i++) {
			Data data = handlers[i];
			try {
				data.handle.invokeExact(data.source, event);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}
