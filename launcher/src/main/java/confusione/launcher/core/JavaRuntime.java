package confusione.launcher.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Java 17 for Minecraft 1.20.1. Uses the launcher's own JVM when it is 17 or newer; otherwise
 * downloads Mojang's official "java-runtime-gamma" (the runtime the vanilla launcher uses)
 * into &lt;gameDir&gt;/runtime once, and reuses it.
 */
public final class JavaRuntime {

	private static final String ALL = "https://launchermeta.mojang.com/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json";
	private static final String COMPONENT = "java-runtime-gamma";

	private JavaRuntime() {}

	static String platform() {
		if (OS.NAME.equals("windows")) {
			return OS.ARM ? "windows-arm64" : "windows-x64";
		}
		if (OS.NAME.equals("osx")) {
			return OS.ARM ? "mac-os-arm64" : "mac-os";
		}
		return OS.ARM ? null : "linux";
	}

	public static String ensure17(File gameDir, Progress p) throws IOException {
		if (GameLauncher.javaMajor() >= 17) {
			return new File(System.getProperty("java.home"), "bin/" + (OS.NAME.equals("windows") ? "javaw.exe" : "java")).getPath();
		}
		String platform = platform();
		if (platform == null) {
			throw new IOException("Minecraft 1.20.1 needs Java 17. Mojang has no Java 17 build for this platform; run the launcher with Java 17.");
		}
		File home = new File(gameDir, "runtime/" + COMPONENT + "/" + platform);
		File java = executable(home);
		File done = new File(home, ".confu-complete");
		if (java.isFile() && done.isFile()) {
			return java.getPath();
		}
		p.stage("Java 17 runtime (Mojang)");
		p.progress(-1);
		Map<String, Object> all = Json.obj(Json.parse(Http.getText(ALL)));
		List<Object> entries = Json.arr(Json.obj(all.get(platform)).get(COMPONENT));
		if (entries == null || entries.isEmpty()) {
			throw new IOException("Mojang lists no " + COMPONENT + " for " + platform);
		}
		Map<String, Object> manifestRef = Json.obj(Json.obj(entries.get(0)).get("manifest"));
		Map<String, Object> files = Json.obj(Json.obj(Json.parse(Http.getText(Json.str(manifestRef, "url")))).get("files"));
		List<String[]> links = new ArrayList<String[]>();
		int total = files.size();
		int n = 0;
		for (Map.Entry<String, Object> e : files.entrySet()) {
			Map<String, Object> f = Json.obj(e.getValue());
			File target = new File(home, e.getKey());
			String type = Json.str(f, "type");
			if ("directory".equals(type)) {
				target.mkdirs();
			} else if ("file".equals(type)) {
				Map<String, Object> raw = Json.obj(Json.obj(f.get("downloads")).get("raw"));
				Http.download(Json.str(raw, "url"), target, Json.str(raw, "sha1"), null);
				if (Boolean.TRUE.equals(f.get("executable"))) {
					target.setExecutable(true, false);
				}
			} else if ("link".equals(type)) {
				links.add(new String[] { e.getKey(), Json.str(f, "target") });
			}
			p.progress(++n / (double) total);
		}
		for (String[] l : links) {
			File link = new File(home, l[0]);
			if (!link.exists()) {
				link.getParentFile().mkdirs();
				try {
					Files.createSymbolicLink(link.toPath(), new File(l[1]).toPath());
				} catch (Exception ex) {
					p.log("Could not link " + l[0] + ": " + ex.getMessage());
				}
			}
		}
		if (!java.isFile()) {
			throw new IOException("Java runtime downloaded but " + java + " is missing");
		}
		Files.write(done.toPath(), "ok".getBytes(StandardCharsets.UTF_8));
		p.log("Java 17 runtime ready: " + java);
		return java.getPath();
	}

	private static File executable(File home) {
		if (OS.NAME.equals("windows")) {
			return new File(home, "bin/javaw.exe");
		}
		if (OS.NAME.equals("osx")) {
			return new File(home, "jre.bundle/Contents/Home/bin/java");
		}
		return new File(home, "bin/java");
	}
}
