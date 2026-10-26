package confusione.launcher.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Builds and runs the LaunchWrapper command for 1.8.9 + the CONFUSION3H tweaker. */
public final class GameLauncher {

	private GameLauncher() {}

	public static String javaExecutable(Settings s) {
		if (s.javaPath != null && !s.javaPath.isEmpty()) {
			return s.javaPath;
		}
		boolean win = OS.NAME.equals("windows");
		File home = new File(System.getProperty("java.home"), "bin/" + (win ? "javaw.exe" : "java"));
		if (home.isFile() || !MacFocus.isMac()) {
			return home.getPath();
		}
		File bundled = new File(launcherDir(), "jre/bin/java");
		return bundled.isFile() ? bundled.getPath() : home.getPath();
	}

	/** Folder holding the launcher jar. In the macOS app that is Contents/Resources. */
	static File launcherDir() {
		try {
			File jar = new File(GameLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			return jar.isDirectory() ? jar : jar.getParentFile();
		} catch (Exception e) {
			return null;
		}
	}

	/** The Confu icon bundled beside the launcher jar, or null outside the macOS app. */
	static File macIcon() {
		File dir = launcherDir();
		if (dir == null) {
			return null;
		}
		File icns = new File(dir, "confusion3h.icns");
		return icns.isFile() ? icns : null;
	}

	/** Offline-mode UUID, same derivation as vanilla servers use for offline players. */
	public static UUID offlineUuid(String name) {
		return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
	}

	public static List<String> command(Settings s, GameInstaller.Result r, File clientJar, MicrosoftAuth.Session session) {
		return command(s, r, clientJar, session, null);
	}

	public static List<String> command(Settings s, GameInstaller.Result r, File clientJar, MicrosoftAuth.Session session, String fpsBenchSeconds) {
		File gameDir = new File(s.gameDir);
		List<String> cmd = new ArrayList<String>();
		cmd.add(javaExecutable(s));
		// macOS: no -XstartOnFirstThread, as vanilla 1.8.9 does. Vanilla uses LWJGL
		// 2.9.2 on osx, which marshals window creation onto the AppKit thread itself.
		// Loom sets that flag only for LWJGL 3 versions.
		if (OS.NAME.equals("osx")) {
			cmd.add("-Dapple.awt.application.name=Confu Client");
			cmd.add("-Xdock:name=Confu Client");
			File icon = macIcon();
			if (icon != null) {
				cmd.add("-Xdock:icon=" + icon.getAbsolutePath());
			}
		}
		cmd.add("-Xmx" + s.ramMb + "M");
		cmd.add("-Xms" + Math.min(1024, s.ramMb) + "M");
		// G1 tuned for a game loop: short pauses, a roomy young gen for the many
		// short-lived per-frame objects 1.8.9 allocates, and no explicit-GC stalls
		cmd.add("-XX:+UseG1GC");
		cmd.add("-XX:+UnlockExperimentalVMOptions");
		cmd.add("-XX:MaxGCPauseMillis=50");
		cmd.add("-XX:G1NewSizePercent=20");
		cmd.add("-XX:G1ReservePercent=20");
		cmd.add("-XX:G1HeapRegionSize=16M");
		cmd.add("-XX:+ParallelRefProcEnabled");
		cmd.add("-XX:+DisableExplicitGC");
		cmd.add("-XX:-UseBiasedLocking"); // fewer safepoint pauses from revocation
		cmd.add("-XX:+AlwaysPreTouch");
		cmd.add("-XX:+UseStringDeduplication");
		cmd.add("-XX:+PerfDisableSharedMem");
		cmd.add("-XX:ReservedCodeCacheSize=256m");
		if (javaMajor() >= 9) {
			addModernJvm(cmd);
		}
		String natives = r.nativesDir.getAbsolutePath();
		cmd.add("-Djava.library.path=" + natives);
		// LWJGL 2 prefers this over java.library.path when both are set. On Apple
		// Silicon the directory is the ARM overlay (liblwjgl.dylib, openal.dylib).
		cmd.add("-Dorg.lwjgl.librarypath=" + natives);
		cmd.add("-Dminecraft.launcher.brand=Confu Client");
		if (fpsBenchSeconds != null && !fpsBenchSeconds.isEmpty()) {
			cmd.add("-Dconfu.fpsbench=" + fpsBenchSeconds);
		}
		cmd.add("-cp");
		StringBuilder cp = new StringBuilder();
		// compat jar first: patched Launch and the Mixin class-byte fallback
		File compat = LaunchCompat.compatJar();
		if (compat != null) {
			cp.append(compat.getAbsolutePath()).append(File.pathSeparatorChar);
		}
		// client next: its embedded Mixin and LWJGL3/NanoVG classes must win over vanilla
		cp.append(clientJar.getAbsolutePath());
		File optifine = s.optifine ? OptiFine.installed(new File(s.gameDir)) : null;
		if (optifine != null) {
			cp.append(File.pathSeparatorChar).append(optifine.getAbsolutePath());
		}
		for (File f : r.classpath) {
			cp.append(File.pathSeparatorChar).append(f.getAbsolutePath());
		}
		cp.append(File.pathSeparatorChar).append(r.gameJar.getAbsolutePath());
		cmd.add(cp.toString());
		cmd.add("net.minecraft.launchwrapper.Launch");
		String username = session != null ? session.name : s.username;
		String uuid = session != null ? session.uuid : offlineUuid(s.username).toString().replace("-", "");
		String token = session != null ? session.accessToken : "0";
		String userType = session != null ? "msa" : "legacy";
		cmd.add("--username");
		cmd.add(username);
		cmd.add("--version");
		cmd.add(GameInstaller.VERSION);
		cmd.add("--gameDir");
		cmd.add(gameDir.getAbsolutePath());
		cmd.add("--assetsDir");
		cmd.add(r.assetsDir.getAbsolutePath());
		cmd.add("--assetIndex");
		cmd.add(r.assetIndex);
		cmd.add("--uuid");
		cmd.add(uuid);
		cmd.add("--accessToken");
		cmd.add(token);
		cmd.add("--userProperties");
		cmd.add("{}");
		cmd.add("--userType");
		cmd.add(userType);
		cmd.add("--tweakClass");
		cmd.add(ClientLocator.tweakClass(clientJar));
		return cmd;
	}

	public static Process start(List<String> cmd, File gameDir, final Progress p) throws IOException {
		gameDir.mkdirs();
		final Process proc = new ProcessBuilder(cmd).directory(gameDir).redirectErrorStream(true).start();
		Thread pump = new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));
					String line;
					while ((line = r.readLine()) != null) {
						p.log(line);
					}
				} catch (IOException ignored) {
				}
			}
		}, "game-output");
		pump.setDaemon(true);
		pump.start();
		return proc;
	}

	static int javaMajor() {
		String v = System.getProperty("java.specification.version", "1.8");
		if (v.startsWith("1.")) {
			return Integer.parseInt(v.substring(2));
		}
		int dot = v.indexOf('.');
		return Integer.parseInt(dot < 0 ? v : v.substring(0, dot));
	}

	/** Java 9+ flags that 1.8.9 + Mixin 0.7 need. Ignored on Java 8. */
	private static void addModernJvm(List<String> cmd) {
		cmd.add("--add-modules");
		cmd.add("jdk.httpserver");
		String[] opens = {
			"java.base/java.lang",
			"java.base/java.lang.reflect",
			"java.base/java.util",
			"java.base/java.io",
			"java.base/java.net",
			"java.base/jdk.internal.loader",
			"java.base/jdk.internal.misc",
			"java.base/sun.nio.ch",
		};
		for (String pkg : opens) {
			cmd.add("--add-opens");
			cmd.add(pkg + "=ALL-UNNAMED");
		}
		File rt = LaunchCompat.java8Rt();
		if (rt != null) {
			cmd.add("-Dconfu.java8.rt=" + rt.getAbsolutePath());
		}
	}

	/** For display: quotes args with spaces. */
	public static String toShell(List<String> cmd) {
		StringBuilder b = new StringBuilder();
		boolean redactNext = false;
		for (String a : cmd) {
			if (b.length() > 0) {
				b.append(' ');
			}
			String shown = a;
			if (redactNext && !"0".equals(a)) {
				shown = "<redacted>";
			}
			redactNext = "--accessToken".equals(a);
			b.append(shown.contains(" ") || shown.contains("{") ? "'" + shown + "'" : shown);
		}
		return b.toString();
	}

	/** OptiFine is optional: a failed download is logged and the game starts without it. */
	public static void fetchOptiFine(Settings s, Progress p) {
		if (!s.optifine) {
			return;
		}
		try {
			OptiFine.ensure(new File(s.gameDir), p);
		} catch (Exception e) {
			p.log("OptiFine not loaded: " + e.getMessage());
		}
	}
}
