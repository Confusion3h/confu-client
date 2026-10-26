package confusione.launcher.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Minecraft 1.20.1 on Fabric: vanilla files from Mojang (shared libraries/assets with 1.8.9),
 * Fabric Loader libraries from meta.fabricmc.net / maven.fabricmc.net, Fabric API from
 * maven.fabricmc.net, and the bundled Confu mod jar copied into the instance's mods folder.
 */
public final class ModernInstaller {

	public static final String MC = "1.20.1";
	public static final String LOADER = "0.20.1";
	public static final String FABRIC_API = "0.92.12+1.20.1";
	private static final String META = "https://meta.fabricmc.net/v2/versions/loader/" + MC + "/" + LOADER + "/profile/json";
	private static final String FABRIC_MAVEN = "https://maven.fabricmc.net/";

	public static final class Result {
		public GameInstaller.Result vanilla;
		public final List<File> fabricLibs = new ArrayList<File>();
		public String mainClass;
		public final List<String> jvmArgs = new ArrayList<String>();
		public File instanceDir;
	}

	private final File gameDir;
	private final Progress p;

	public ModernInstaller(File gameDir, Progress p) {
		this.gameDir = gameDir;
		this.p = p;
	}

	/** Separate game directory so 1.20.1 options, worlds and mods never touch the 1.8.9 ones. */
	public static File instanceDir(File gameDir) {
		return new File(gameDir, "modern-" + MC);
	}

	public Result install(File confuModJar) throws IOException {
		Result r = new Result();
		r.vanilla = new GameInstaller(gameDir, p, MC).install();
		r.instanceDir = instanceDir(gameDir);

		p.stage("Fabric Loader " + LOADER);
		p.progress(-1);
		File profileFile = new File(gameDir, "versions/fabric-loader-" + LOADER + "-" + MC + "/profile.json");
		if (!profileFile.isFile()) {
			Http.download(META, profileFile, null, null);
		}
		Map<String, Object> profile = Json.obj(Json.parse(new String(java.nio.file.Files.readAllBytes(profileFile.toPath()), "UTF-8")));
		r.mainClass = Json.str(profile, "mainClass");
		Map<String, Object> args = Json.obj(profile.get("arguments"));
		if (args != null && args.get("jvm") != null) {
			for (Object o : Json.arr(args.get("jvm"))) {
				if (o instanceof String) {
					r.jvmArgs.add((String) o);
				}
			}
		}
		File libDir = new File(gameDir, "libraries");
		for (Object o : Json.arr(profile.get("libraries"))) {
			Map<String, Object> lib = Json.obj(o);
			String path = mavenPath(Json.str(lib, "name"));
			String base = Json.str(lib, "url");
			if (base == null) {
				base = FABRIC_MAVEN;
			}
			if (!base.endsWith("/")) {
				base += "/";
			}
			File f = new File(libDir, path);
			String sha1 = Json.str(lib, "sha1");
			if (sha1 == null && !f.isFile()) {
				sha1 = remoteSha1(base + path);
			}
			if (Http.download(base + path, f, sha1, null) > 0) {
				p.log("Downloaded " + f.getName());
			}
			r.fabricLibs.add(f);
		}

		p.stage("Fabric API " + FABRIC_API);
		File mods = new File(r.instanceDir, "mods");
		mods.mkdirs();
		String apiPath = mavenPath("net.fabricmc.fabric-api:fabric-api:" + FABRIC_API);
		File api = new File(mods, "fabric-api-" + FABRIC_API + ".jar");
		if (!api.isFile()) {
			Http.download(FABRIC_MAVEN + apiPath, api, remoteSha1(FABRIC_MAVEN + apiPath), null);
			p.log("Downloaded " + api.getName());
		}

		File[] old = mods.listFiles();
		if (old != null) {
			for (File f : old) {
				if (f.getName().startsWith("confu-modern") && f.getName().endsWith(".jar")) {
					f.delete();
				}
			}
		}
		copy(confuModJar, new File(mods, confuModJar.getName()));
		p.log("Confu mod: " + confuModJar.getName());
		p.progress(1);
		p.stage("Game files ready");
		return r;
	}

	private String remoteSha1(String url) {
		try {
			return Http.getText(url + ".sha1").trim().split("\\s+")[0];
		} catch (IOException e) {
			p.log("No .sha1 for " + url + ", downloading unverified");
			return null;
		}
	}

	/** group:artifact:version[:classifier] to a maven repository path. */
	static String mavenPath(String name) {
		String[] parts = name.split(":");
		String file = parts[1] + "-" + parts[2] + (parts.length > 3 ? "-" + parts[3] : "") + ".jar";
		return parts[0].replace('.', '/') + "/" + parts[1] + "/" + parts[2] + "/" + file;
	}

	private static void copy(File from, File to) throws IOException {
		InputStream in = new FileInputStream(from);
		try {
			OutputStream out = new FileOutputStream(to);
			try {
				byte[] buf = new byte[16384];
				int n;
				while ((n = in.read(buf)) != -1) {
					out.write(buf, 0, n);
				}
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}

	public static List<String> command(Settings s, Result r, MicrosoftAuth.Session session) {
		List<String> cmd = new ArrayList<String>();
		cmd.add(GameLauncher.javaExecutable(s));
		if (OS.NAME.equals("osx")) {
			cmd.add("-XstartOnFirstThread");
			cmd.add("-Dapple.awt.application.name=Confu Client");
			cmd.add("-Xdock:name=Confu Client");
		}
		cmd.add("-Xmx" + s.ramMb + "M");
		cmd.add("-Xms" + Math.min(1024, s.ramMb) + "M");
		cmd.add("-XX:+UseG1GC");
		cmd.add("-XX:+UnlockExperimentalVMOptions");
		cmd.add("-XX:MaxGCPauseMillis=50");
		cmd.add("-XX:G1NewSizePercent=20");
		cmd.add("-XX:G1ReservePercent=20");
		cmd.add("-XX:G1HeapRegionSize=16M");
		cmd.add("-Djava.library.path=" + r.vanilla.nativesDir.getAbsolutePath());
		cmd.add("-Dminecraft.launcher.brand=Confu Client");
		cmd.addAll(r.jvmArgs);
		cmd.add("-cp");
		StringBuilder cp = new StringBuilder();
		for (File f : r.fabricLibs) {
			cp.append(f.getAbsolutePath()).append(File.pathSeparatorChar);
		}
		for (File f : r.vanilla.classpath) {
			cp.append(f.getAbsolutePath()).append(File.pathSeparatorChar);
		}
		cp.append(r.vanilla.gameJar.getAbsolutePath());
		cmd.add(cp.toString());
		cmd.add(r.mainClass);
		String username = session != null ? session.name : s.username;
		String uuid = session != null ? session.uuid : GameLauncher.offlineUuid(s.username).toString().replace("-", "");
		String token = session != null ? session.accessToken : "0";
		cmd.add("--username");
		cmd.add(username);
		cmd.add("--version");
		cmd.add("fabric-loader-" + LOADER + "-" + MC);
		cmd.add("--gameDir");
		cmd.add(r.instanceDir.getAbsolutePath());
		cmd.add("--assetsDir");
		cmd.add(r.vanilla.assetsDir.getAbsolutePath());
		cmd.add("--assetIndex");
		cmd.add(r.vanilla.assetIndex);
		cmd.add("--uuid");
		cmd.add(uuid);
		cmd.add("--accessToken");
		cmd.add(token);
		cmd.add("--userType");
		cmd.add(session != null ? "msa" : "legacy");
		cmd.add("--versionType");
		cmd.add("release");
		String quick = System.getProperty("confu.quickPlay");
		if (quick != null && !quick.isEmpty()) {
			cmd.add("--quickPlayMultiplayer");
			cmd.add(quick);
		}
		return cmd;
	}
}
