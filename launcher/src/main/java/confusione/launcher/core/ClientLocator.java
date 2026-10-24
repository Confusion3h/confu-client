package confusione.launcher.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Finds the CONFUSION3H client jar (the ForgeGradle tweaker-client output in build/libs)
 * and the tweak class from its manifest; can build it with the repo's gradlew.
 */
public final class ClientLocator {

	/** Matches build.gradle's minecraft.tweakClass; used if the manifest lacks TweakClass. */
	public static final String DEFAULT_TWEAKER = "me.eldodebug.soar.injection.mixin.GlideTweaker";

	private ClientLocator() {}

	/** Explicit path, then -Dconfusione.client, then build/libs of a repo above cwd or the launcher jar. */
	public static File find(String configured) {
		if (configured != null && !configured.isEmpty()) {
			File f = new File(configured);
			return f.isFile() ? f : null;
		}
		String prop = System.getProperty("confusione.client");
		if (prop != null && new File(prop).isFile()) {
			return new File(prop);
		}
		File installed = installedJar();
		if (installed != null) {
			return installed;
		}
		File repo = findRepo();
		if (repo != null) {
			File[] jars = new File(repo, "build/libs").listFiles();
			File best = null;
			if (jars != null) {
				for (File j : jars) {
					String n = j.getName();
					if (n.endsWith(".jar") && !n.contains("-sources") && !n.contains("-dev")
							&& (best == null || j.lastModified() > best.lastModified())) {
						best = j;
					}
				}
			}
			return best;
		}
		return null;
	}

	/** The client repo: a directory with gradlew and the client sources, searched upwards. */
	public static File findRepo() {
		List<File> starts = new ArrayList<File>();
		starts.add(new File(System.getProperty("user.dir")).getAbsoluteFile());
		try {
			starts.add(new File(ClientLocator.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsoluteFile());
		} catch (Exception ignored) {
		}
		for (File d : starts) {
			for (; d != null; d = d.getParentFile()) {
				if (new File(d, "gradlew").isFile() && new File(d, "src/main/java/me/eldodebug/soar").isDirectory()) {
					return d;
				}
			}
		}
		return null;
	}

	/** Installed layout (Windows installer): <dir>/client/CONFUSION3H-Release.jar beside the launcher jar. */
	static File installedJar() {
		try {
			File self = new File(ClientLocator.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			File f = new File(self.getParentFile(), "client" + File.separator + "CONFUSION3H-Release.jar");
			return f.isFile() ? f : null;
		} catch (Exception e) {
			return null;
		}
	}

	public static String tweakClass(File clientJar) {
		try {
			JarFile jar = new JarFile(clientJar);
			try {
				Manifest m = jar.getManifest();
				String t = m == null ? null : m.getMainAttributes().getValue("TweakClass");
				return t != null ? t : DEFAULT_TWEAKER;
			} finally {
				jar.close();
			}
		} catch (IOException e) {
			return DEFAULT_TWEAKER;
		}
	}

	/** Runs `gradlew build` in the repo with the given JDK 8 home. Returns exit code. */
	public static int build(File repo, String javaHome, Progress p) throws IOException, InterruptedException {
		List<String> cmd = new ArrayList<String>();
		boolean win = OS.NAME.equals("windows");
		cmd.add(new File(repo, win ? "gradlew.bat" : "gradlew").getAbsolutePath());
		cmd.add("build");
		// assets are only needed by runClient; the launcher fetches them itself over HTTPS
		cmd.add("-x");
		cmd.add("getAssets");
		cmd.add("--no-daemon");
		ProcessBuilder pb = new ProcessBuilder(cmd).directory(repo).redirectErrorStream(true);
		if (javaHome != null) {
			pb.environment().put("JAVA_HOME", javaHome);
		}
		p.stage("Building client (gradlew build)");
		p.progress(-1);
		Process proc = pb.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line;
		while ((line = r.readLine()) != null) {
			p.log(line);
		}
		return proc.waitFor();
	}
}
