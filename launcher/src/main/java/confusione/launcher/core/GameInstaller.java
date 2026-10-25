package confusione.launcher.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Prepares a vanilla 1.8.9 game directory from Mojang's HTTPS metadata, plus the
 * LaunchWrapper + ASM libraries the CONFUSION3H tweaker needs (vanilla 1.8.9 has neither).
 */
public final class GameInstaller {

	public static final String VERSION = "1.8.9";
	private static final String MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
	private static final String RESOURCES = "https://resources.download.minecraft.net/";

	/** {maven path, base url}. sha1 is read from the repo's .sha1 sidecar. */
	private static final String[][] EXTRA_LIBS = {
		{ "net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar", "https://libraries.minecraft.net/" },
		{ "org/ow2/asm/asm-all/5.0.3/asm-all-5.0.3.jar", "https://repo1.maven.org/maven2/" },
		// Java 11+ removed javax.annotation.Resource. Mixin 0.7 asks for it while applying.
		{ "javax/annotation/javax.annotation-api/1.3.2/javax.annotation-api-1.3.2.jar", "https://repo1.maven.org/maven2/" },
	};

	public static final class Result {
		public final List<File> classpath = new ArrayList<File>();
		public File gameJar;
		public File nativesDir;
		public File assetsDir;
		public String assetIndex;
		public String mainClassVanilla;
	}

	private final File gameDir;
	private final Progress p;

	public GameInstaller(File gameDir, Progress progress) {
		this.gameDir = gameDir;
		this.p = progress;
	}

	public Result install() throws IOException {
		Result r = new Result();
		File versionDir = new File(gameDir, "versions/" + VERSION);
		File versionJson = new File(versionDir, VERSION + ".json");

		p.stage("Reading version manifest");
		p.progress(-1);
		if (!versionJson.isFile()) {
			Map<String, Object> manifest = Json.obj(Json.parse(Http.getText(MANIFEST)));
			String url = null;
			String sha1 = null;
			for (Object o : Json.arr(manifest.get("versions"))) {
				Map<String, Object> v = Json.obj(o);
				if (VERSION.equals(Json.str(v, "id"))) {
					url = Json.str(v, "url");
					sha1 = Json.str(v, "sha1");
				}
			}
			if (url == null) {
				throw new IOException(VERSION + " not in Mojang manifest");
			}
			Http.download(url, versionJson, sha1, null);
			p.log("Saved " + versionJson);
		}
		Map<String, Object> version = Json.obj(Json.parse(new String(Files.readAllBytes(versionJson.toPath()), StandardCharsets.UTF_8)));
		r.mainClassVanilla = Json.str(version, "mainClass");

		// work list: libraries + client jar
		List<String[]> jobs = new ArrayList<String[]>(); // url, file, sha1
		List<File[]> natives = new ArrayList<File[]>();   // jar, (unused)
		File libDir = new File(gameDir, "libraries");

		for (Object o : Json.arr(version.get("libraries"))) {
			Map<String, Object> lib = Json.obj(o);
			if (!OS.allowed(lib.get("rules"))) {
				continue;
			}
			Map<String, Object> dl = Json.obj(lib.get("downloads"));
			if (dl == null) {
				continue;
			}
			if (dl.containsKey("artifact") && !lib.containsKey("natives")) {
				Map<String, Object> a = Json.obj(dl.get("artifact"));
				File f = new File(libDir, Json.str(a, "path"));
				jobs.add(new String[] { Json.str(a, "url"), f.getPath(), Json.str(a, "sha1") });
				r.classpath.add(f);
			}
			if (lib.containsKey("natives")) {
				String key = Json.str(Json.obj(lib.get("natives")), OS.NAME);
				if (key != null) {
					key = key.replace("${arch}", OS.ARCH_BITS);
					Map<String, Object> a = Json.obj(Json.obj(dl.get("classifiers")).get(key));
					if (a != null) {
						File f = new File(libDir, Json.str(a, "path"));
						jobs.add(new String[] { Json.str(a, "url"), f.getPath(), Json.str(a, "sha1") });
						natives.add(new File[] { f });
					}
				}
			}
		}

		for (String[] extra : EXTRA_LIBS) {
			File f = new File(libDir, extra[0]);
			String url = extra[1] + extra[0];
			String sha1 = null;
			if (!f.isFile()) {
				try {
					sha1 = Http.getText(url + ".sha1").trim().split("\\s+")[0];
				} catch (IOException e) {
					p.log("No .sha1 for " + url + ", downloading unverified");
				}
			}
			jobs.add(new String[] { url, f.getPath(), sha1 });
			r.classpath.add(f);
		}

		Map<String, Object> clientDl = Json.obj(Json.obj(version.get("downloads")).get("client"));
		r.gameJar = new File(versionDir, VERSION + ".jar");
		jobs.add(new String[] { Json.str(clientDl, "url"), r.gameJar.getPath(), Json.str(clientDl, "sha1") });

		p.stage("Libraries (" + jobs.size() + " files)");
		runParallel(jobs, 0.0, 0.35, true);

		p.stage("Extracting natives");
		r.nativesDir = new File(versionDir, "natives");
		r.nativesDir.mkdirs();
		for (File[] n : natives) {
			extractNatives(n[0], r.nativesDir);
		}
		overlayArmNatives(r.nativesDir);

		// assets
		Map<String, Object> ai = Json.obj(version.get("assetIndex"));
		r.assetIndex = Json.str(ai, "id");
		r.assetsDir = new File(gameDir, "assets");
		File indexFile = new File(r.assetsDir, "indexes/" + r.assetIndex + ".json");
		p.stage("Asset index " + r.assetIndex);
		Http.download(Json.str(ai, "url"), indexFile, Json.str(ai, "sha1"), null);
		Map<String, Object> objects = Json.obj(Json.obj(Json.parse(new String(Files.readAllBytes(indexFile.toPath()), StandardCharsets.UTF_8))).get("objects"));
		List<String[]> assetJobs = new ArrayList<String[]>();
		Set<String> seen = new HashSet<String>();
		int shared = 0;
		for (Object o : objects.values()) {
			Map<String, Object> obj = Json.obj(o);
			String hash = Json.str(obj, "hash");
			String rel = hash.substring(0, 2) + "/" + hash;
			File f = new File(r.assetsDir, "objects/" + rel);
			// Several names (icons/minecraft.icns and minecraft/icons/minecraft.icns) share one
			// hash. Queueing both made two workers write the same .part and fail SHA-1.
			if (!seen.add(f.getAbsolutePath())) {
				shared++;
				continue;
			}
			long size = Json.num(obj, "size");
			if (f.isFile() && f.length() == size && hash.equalsIgnoreCase(Http.sha1(f))) {
				continue;
			}
			if (f.isFile()) {
				f.delete();
			}
			assetJobs.add(new String[] { RESOURCES + rel, f.getPath(), hash });
		}
		if (shared > 0) {
			p.log("Asset index reuses " + shared + " files; each is downloaded once");
		}
		p.stage("Assets (" + assetJobs.size() + " of " + objects.size() + " to fetch)");
		int skipped = runParallel(assetJobs, 0.35, 1.0, false);
		if (skipped > 0) {
			p.log("WARNING: " + skipped + " asset(s) failed SHA-1 checks after retries and were skipped. Minecraft can still start; those sounds or textures will be missing.");
		}

		p.progress(1);
		p.stage(skipped > 0 ? "Game files ready (" + skipped + " asset(s) skipped)" : "Game files ready");
		return r;
	}

	/** @param required when false, a download that still fails after retries is logged and skipped
	 *  @return number of skipped jobs */
	private int runParallel(final List<String[]> jobs, final double from, final double to, final boolean required) throws IOException {
		if (jobs.isEmpty()) {
			p.progress(to);
			return 0;
		}
		ExecutorService pool = Executors.newFixedThreadPool(8);
		final List<String> errors = Collections.synchronizedList(new ArrayList<String>());
		try {
			final int[] done = { 0 };
			List<Future<?>> futures = new ArrayList<Future<?>>();
			for (final String[] j : jobs) {
				futures.add(pool.submit(new java.util.concurrent.Callable<Void>() {
					public Void call() throws IOException {
						try {
							long n = Http.download(j[0], new File(j[1]), j[2], null);
							if (n > 0 && !j[1].contains("objects")) {
								p.log("Downloaded " + new File(j[1]).getName());
							}
						} catch (IOException e) {
							if (required) {
								throw e;
							}
							errors.add(j[0] + " — " + e.getMessage());
							p.log("ERROR: asset skipped after retries: " + j[0]);
							p.log("       " + e.getMessage());
						} finally {
							synchronized (done) {
								done[0]++;
								p.progress(from + (to - from) * done[0] / jobs.size());
							}
						}
						return null;
					}
				}));
			}
			for (Future<?> f : futures) {
				try {
					f.get();
				} catch (java.util.concurrent.ExecutionException e) {
					Throwable c = e.getCause();
					throw c instanceof IOException ? (IOException) c : new IOException(c);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IOException("Interrupted");
				}
			}
		} finally {
			pool.shutdownNow();
		}
		return errors.size();
	}

	/** Mojang ships x86_64 natives only. ARM packages drop LWJGL2 binaries in natives/&lt;os&gt;-arm64. */
	private void overlayArmNatives(File dir) throws IOException {
		File bundle = LaunchCompat.armNatives();
		if (bundle == null) {
			if (OS.ARM) {
				File expected = new File(LaunchCompat.installDir(), "natives" + File.separator + OS.NAME + "-arm64");
				p.log("ARM natives were not found at " + expected.getAbsolutePath()
						+ ". Mojang's x86_64 libraries cannot open a window on this machine.");
			}
			return;
		}
		File[] files = bundle.listFiles();
		if (files == null) {
			return;
		}
		int n = 0;
		for (File f : files) {
			if (!f.isFile()) {
				continue;
			}
			copyFile(f, new File(dir, f.getName()));
			n++;
		}
		p.log("Overlaid " + n + " ARM natives from " + bundle.getAbsolutePath());
	}

	private static void copyFile(File from, File to) throws IOException {
		InputStream in = new java.io.FileInputStream(from);
		try {
			OutputStream os = new FileOutputStream(to);
			try {
				byte[] buf = new byte[16384];
				int n;
				while ((n = in.read(buf)) != -1) {
					os.write(buf, 0, n);
				}
			} finally {
				os.close();
			}
		} finally {
			in.close();
		}
	}

	private void extractNatives(File jar, File dir) throws IOException {
		ZipFile zip = new ZipFile(jar);
		try {
			java.util.Enumeration<? extends ZipEntry> en = zip.entries();
			while (en.hasMoreElements()) {
				ZipEntry e = en.nextElement();
				if (e.isDirectory() || e.getName().startsWith("META-INF/")) {
					continue;
				}
				File out = new File(dir, new File(e.getName()).getName());
				if (out.isFile() && out.length() == e.getSize()) {
					continue;
				}
				InputStream in = zip.getInputStream(e);
				try {
					OutputStream os = new FileOutputStream(out);
					try {
						byte[] buf = new byte[16384];
						int n;
						while ((n = in.read(buf)) != -1) {
							os.write(buf, 0, n);
						}
					} finally {
						os.close();
					}
				} finally {
					in.close();
				}
			}
		} finally {
			zip.close();
		}
	}
}
