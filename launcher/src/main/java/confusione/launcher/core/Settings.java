package confusione.launcher.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/** ~/.confusione/launcher.properties */
public final class Settings {

	public static final File HOME = new File(System.getProperty("user.home"), ".confusione");
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
		s.gameDir = p.getProperty("gameDir", s.gameDir);
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
				p.store(out, "confusione launcher");
			} finally {
				out.close();
			}
		} catch (IOException ignored) {
		}
	}
}
