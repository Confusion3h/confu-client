package confusione.launcher.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/** ~/.CONFUSION3H/launcher.properties (moved from the phase 2 ~/.confusione on first run) */
public final class Settings {

	private static final File LEGACY_HOME = new File(System.getProperty("user.home"), ".confusione");
	public static final File HOME = migrate(new File(System.getProperty("user.home"), ".CONFUSION3H"));

	private static File migrate(File home) {
		if (!home.exists() && LEGACY_HOME.isDirectory()) {
			LEGACY_HOME.renameTo(home); // same parent dir: an atomic rename, keeps the installed game
		}
		return home;
	}

	/** Paths saved before the rename still point into ~/.confusione. */
	private static String rebase(String path) {
		String old = LEGACY_HOME.getPath();
		if (path != null && !LEGACY_HOME.exists() && (path.equals(old) || path.startsWith(old + File.separator))) {
			return HOME.getPath() + path.substring(old.length());
		}
		return path;
	}
	private static final File FILE = new File(HOME, "launcher.properties");

	public String username = "Player";
	public int ramMb = 2048;
	public String javaPath = "";      // empty: the JVM running the launcher
	public String gameDir = new File(HOME, "minecraft").getPath();
	public String clientJar = "";     // empty: auto-detect
	public boolean reduceMotion = false;
	public boolean keepOpen = false;

	public static Settings load() {
		Settings s = new Settings();
		Properties p = new Properties();
		if (FILE.isFile()) {
			try {
				InputStream in = new FileInputStream(FILE);
				try {
					p.load(in);
				} finally {
					in.close();
				}
			} catch (IOException ignored) {
			}
		}
		s.username = p.getProperty("username", s.username);
		s.javaPath = p.getProperty("javaPath", s.javaPath);
		s.gameDir = rebase(p.getProperty("gameDir", s.gameDir));
		s.clientJar = p.getProperty("clientJar", s.clientJar);
		s.reduceMotion = Boolean.parseBoolean(p.getProperty("reduceMotion", "false"));
		s.keepOpen = Boolean.parseBoolean(p.getProperty("keepOpen", "false"));
		try {
			s.ramMb = Integer.parseInt(p.getProperty("ramMb", "2048"));
		} catch (NumberFormatException ignored) {
		}
		return s;
	}

	public void save() {
		Properties p = new Properties();
		p.setProperty("username", username);
		p.setProperty("ramMb", Integer.toString(ramMb));
		p.setProperty("javaPath", javaPath);
		p.setProperty("gameDir", gameDir);
		p.setProperty("clientJar", clientJar);
		p.setProperty("reduceMotion", Boolean.toString(reduceMotion));
		p.setProperty("keepOpen", Boolean.toString(keepOpen));
		try {
			HOME.mkdirs();
			OutputStream out = new FileOutputStream(FILE);
			try {
				p.store(out, "CONFUSION3H launcher");
			} finally {
				out.close();
			}
		} catch (IOException ignored) {
		}
	}
}
