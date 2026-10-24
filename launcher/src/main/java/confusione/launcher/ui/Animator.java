package confusione.launcher.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

/**
 * Single 60 Hz Swing timer shared by every transition. It only runs while some
 * {@link Tickable} reports it is still moving, so a settled UI costs no timer wakeups.
 * Ambient motion (background, logo) runs on its own slower clock in {@link Ambient}.
 */
public final class Animator {

	public interface Tickable {
		/** Advance by dt seconds; return true while still animating. */
		boolean tick(float dt);
	}

	private static final List<Tickable> active = new ArrayList<Tickable>();
	private static long last;
	private static final Timer timer = new Timer(16, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			long now = System.nanoTime();
			float dt = Math.min(0.05f, (now - last) / 1e9f);
			last = now;
			for (int i = active.size() - 1; i >= 0; i--) {
				if (!active.get(i).tick(dt)) {
					active.remove(i);
				}
			}
			if (active.isEmpty()) {
				timer.stop();
			}
		}
	});

	static {
		timer.setCoalesce(true);
	}

	private Animator() {}

	/** Ensure t is ticked until it settles. Call on the EDT. */
	public static void wake(Tickable t) {
		if (!active.contains(t)) {
			active.add(t);
		}
		if (!timer.isRunning()) {
			last = System.nanoTime();
			timer.start();
		}
	}

	public static boolean isRunning() {
		return timer.isRunning();
	}
}
