package me.eldodebug.soar.management.notification;

import java.util.concurrent.LinkedBlockingQueue;

import me.eldodebug.soar.management.language.TranslateText;

/** The 1.8.9 toast queue: one notification at a time, bottom right, drawn after everything else. */
public class NotificationManager {

	private final LinkedBlockingQueue<Notification> notifications = new LinkedBlockingQueue<Notification>();
	private Notification current;

	public void post(TranslateText title, TranslateText message, NotificationType type) {
		notifications.add(new Notification(title, message, type));
	}

	public void post(String title, String message, NotificationType type) {
		notifications.add(new Notification(title, message, type));
	}

	/** Render thread, once per frame (GameRenderer.render tail). */
	public void render() {
		if (current != null && !current.isShown()) {
			current = null;
		}
		if (current == null && !notifications.isEmpty()) {
			current = notifications.poll();
			current.show();
		}
		if (current != null) {
			current.draw();
		}
	}
}
