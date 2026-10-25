package confusione.launcher.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Brings the game window to the front on macOS. A process the launcher starts is
 * not the active app, and since macOS 14 a window it opens can stay behind the
 * active app's windows. System Events can make a process frontmost once the user
 * allows Confu Client to control System Events (Privacy &amp; Security &gt; Automation).
 * This is best effort: nothing here throws, and a refusal is reported in the console.
 */
public final class MacFocus {

	private static final long[] RETRY_DELAYS_MS = { 1500L, 2500L, 3000L, 4000L };

	private MacFocus() {}

	public static boolean isMac() {
		return OS.NAME.equals("osx");
	}

	/** Raises the game once its window exists. Stops when the game exits or is frontmost. */
	public static void bringToFront(final Process proc, final Progress p) {
		final long pid = pid(proc);
		if (pid <= 0) {
			p.log("The game process id is unknown, so the Minecraft window was not raised. Click it to bring it forward.");
			return;
		}
		Thread t = new Thread(new Runnable() {
			public void run() {
				for (long delay : RETRY_DELAYS_MS) {
					pause(delay);
					if (!proc.isAlive()) {
						return;
					}
					if (isFrontmost(pid)) {
						return;
					}
					Run r = setFrontmost(pid);
					if (denied(r)) {
						p.log(DENIED);
						return;
					}
				}
				if (proc.isAlive()) {
					p.log(NOT_RAISED);
				}
			}
		}, "confu-mac-focus");
		t.setDaemon(true);
		t.start();
	}

	/** Raises the launcher again after the game exits, or when the Dock icon is clicked. */
	public static void raise(final long pid, final Progress p) {
		if (pid <= 0) {
			return;
		}
		Thread t = new Thread(new Runnable() {
			public void run() {
				if (denied(setFrontmost(pid))) {
					p.log(DENIED);
				}
			}
		}, "confu-mac-raise");
		t.setDaemon(true);
		t.start();
	}

	public static long ownPid() {
		try {
			String name = ManagementFactory.getRuntimeMXBean().getName();
			return Long.parseLong(name.substring(0, name.indexOf('@')));
		} catch (RuntimeException e) {
			return -1;
		}
	}

	static long pid(Process proc) {
		try {
			Field f = proc.getClass().getDeclaredField("pid");
			f.setAccessible(true);
			return f.getInt(proc);
		} catch (Throwable t) {
			return -1;
		}
	}

	private static final String DENIED = "macOS has not allowed Confu Client to control System Events, so the window was not brought to the front. Allow it in System Settings > Privacy & Security > Automation.";
	private static final String NOT_RAISED = "The Minecraft window may be behind other windows. Click it, or use Cmd-Tab to reach it.";

	private static boolean isFrontmost(long pid) {
		Run r = run("tell application \"System Events\" to return frontmost of first process whose unix id is " + pid);
		return r != null && r.exit == 0 && "true".equals(r.out.trim());
	}

	private static Run setFrontmost(long pid) {
		return run("tell application \"System Events\" to set frontmost of first process whose unix id is " + pid + " to true");
	}

	private static boolean denied(Run r) {
		return r != null && r.exit != 0
				&& (r.out.contains("-1743") || r.out.contains("Not authorized") || r.out.contains("not allowed"));
	}

	private static Run run(String script) {
		Process proc = null;
		try {
			proc = new ProcessBuilder("osascript", "-e", script).redirectErrorStream(true).start();
			if (!proc.waitFor(20, TimeUnit.SECONDS)) {
				proc.destroy();
				return null;
			}
			return new Run(proc.exitValue(), output(proc));
		} catch (IOException e) {
			return null;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		} finally {
			if (proc != null) {
				proc.destroy();
			}
		}
	}

	private static String output(Process proc) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));
		StringBuilder b = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
			b.append(line).append('\n');
		}
		return b.toString();
	}

	private static void pause(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static final class Run {
		final int exit;
		final String out;

		Run(int exit, String out) {
			this.exit = exit;
			this.out = out;
		}
	}
}
