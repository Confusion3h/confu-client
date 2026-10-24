package confusione.launcher.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Slow clock for decorative motion (background aurora, logo sheen). Runs at
 * IDLE_FPS while the window is focused, BUSY_FPS while a task shows progress, and
 * not at all when the window is unfocused/minimized or reduce-motion is on.
 */
public final class Ambient {

	private static final int IDLE_FPS = 12;
	private static final int BUSY_FPS = 45;

	private static float time;
	private static long last = System.nanoTime();
	private static boolean focused = true;
	private static boolean busy;
	private static boolean reduceMotion;
	private static final List<JComponent> clients = new ArrayList<JComponent>();

	private static final Timer timer = new Timer(1000 / IDLE_FPS, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			long now = System.nanoTime();
			time += Math.min(0.1f, (now - last) / 1e9f);
			last = now;
			for (JComponent c : clients) {
				c.repaint();
			}
		}
	});

	static {
		timer.setCoalesce(true);
	}

	private Ambient() {}

	/** Seconds of ambient time; frozen while paused. */
	public static float time() {
		return time;
	}

	public static void register(JComponent c) {
		clients.add(c);
		update();
	}

	public static void setFocused(boolean f) {
		focused = f;
		update();
	}

	public static void setBusy(boolean b) {
		busy = b;
		update();
	}

	public static void setReduceMotion(boolean r) {
		reduceMotion = r;
		update();
	}

	public static boolean isRunning() {
		return timer.isRunning();
	}

	private static void update() {
		// busy keeps the progress visuals alive even without focus / with reduced motion
		boolean run = busy || (focused && !reduceMotion);
		timer.setDelay(1000 / (busy ? BUSY_FPS : IDLE_FPS));
		if (run && !timer.isRunning()) {
			last = System.nanoTime();
			timer.start();
		} else if (!run && timer.isRunning()) {
			timer.stop();
		}
	}
}
