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
		return new File(System.getProperty("java.home"), "bin/" + (win ? "javaw.exe" : "java")).getPath();
	}

	/** Offline-mode UUID, same derivation as vanilla servers use for offline players. */
	public static UUID offlineUuid(String name) {
		return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
	}

	public static List<String> command(Settings s, GameInstaller.Result r, File clientJar, MicrosoftAuth.Session session) {
		File gameDir = new File(s.gameDir);
		List<String> cmd = new ArrayList<String>();
		cmd.add(javaExecutable(s));
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
		cmd.add("-Djava.library.path=" + r.nativesDir.getAbsolutePath());
		cmd.add("-Dminecraft.launcher.brand=CONFUSION3H");
		if (OS.NAME.equals("osx")) {
			cmd.add("-XstartOnFirstThread");
		}
		cmd.add("-cp");
		StringBuilder cp = new StringBuilder();
		// client first: its embedded Mixin and LWJGL3/NanoVG classes must win
		cp.append(clientJar.getAbsolutePath());
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
}
