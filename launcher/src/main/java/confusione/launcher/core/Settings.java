package confusione.launcher.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/** Data dir is {@code %APPDATA%\.confu} on Windows and {@code ~/.confu} elsewhere. */
public final class Settings {

	private static final File LEGACY_HOME = new File(System.getProperty("user.home"), ".confusione");
	public static final File HOME = resolveHome();

	/** Windows: %APPDATA% (like vanilla .minecraft); elsewhere the home directory. */
	static File baseDir() {
		String appData = System.getenv("APPDATA");
		if (System.getProperty("os.name", "").toLowerCase().contains("win") && appData != null && !appData.isEmpty()) {
			return new File(appData);
		}
		return new File(System.getProperty("user.home"));
	}

	/**
	 * Prefer {@code .confu}. If it is missing, rename a sibling {@code .CONFUSION3H}
	 * (same folder: an atomic move, worlds included). Then the older {@code ~/.confusione}
	 * when it shares that parent. An existing {@code .confu} is left alone.
	 */
	private static File resolveHome() {
		File home = new File(baseDir(), ".confu");
		File previous = new File(baseDir(), ".CONFUSION3H");
		if (!home.exists() && previous.isDirectory()) {
			if (!previous.renameTo(home)) {
				return previous;
			}
		}
		if (!home.exists() && LEGACY_HOME.isDirectory() && sameParent(LEGACY_HOME, home)) {
			if (!LEGACY_HOME.renameTo(home)) {
				return LEGACY_HOME;
			}
		}
		return home;
	}

	private static boolean sameParent(File a, File b) {
		File pa = a.getParentFile();
		File pb = b.getParentFile();
		return pa != null && pb != null && pa.getPath().equals(pb.getPath());
	}

	/** Paths saved before a rename still point at the folder that was moved. */
	private static String rebase(String path) {
		if (path == null) {
			return null;
		}
		path = rebaseOne(path, new File(baseDir(), ".CONFUSION3H"));
		path = rebaseOne(path, LEGACY_HOME);
		return path;
	}

	private static String rebaseOne(String path, File oldDir) {
		String old = oldDir.getPath();
		if (!oldDir.exists() && (path.equals(old) || path.startsWith(old + File.separator))) {
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
	/** Fetch OptiFine from optifine.net on first launch and load it. Off by default. */
	public boolean optifine = false;
	/** Microsoft account instead of the offline name. Refresh token is not stored here. */
	public boolean microsoft = false;
	/** "1.8.9" or "1.20.1" (Modern, Fabric). */
	public String gameVersion = "1.8.9";
	/**
	 * Optional Live client id (16 hex digits). Empty uses the built-in Minecraft client.
	 * Not shown in Settings. An Azure UUID stored by an older build is ignored.
	 */
	public String msClientId = "";

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
		s.optifine = Boolean.parseBoolean(p.getProperty("optifine", "false"));
		s.microsoft = Boolean.parseBoolean(p.getProperty("microsoft", "false"));
		s.msClientId = p.getProperty("msClientId", "").trim();
		s.gameVersion = "1.20.1".equals(p.getProperty("gameVersion")) ? "1.20.1" : "1.8.9";
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
		p.setProperty("optifine", Boolean.toString(optifine));
		p.setProperty("microsoft", Boolean.toString(microsoft));
		p.setProperty("gameVersion", gameVersion);
		p.setProperty("msClientId", msClientId == null ? "" : msClientId.trim());
		try {
			HOME.mkdirs();
			OutputStream out = new FileOutputStream(FILE);
			try {
				p.store(out, "Confu Client launcher");
			} finally {
				out.close();
			}
		} catch (IOException ignored) {
		}
	}
}
