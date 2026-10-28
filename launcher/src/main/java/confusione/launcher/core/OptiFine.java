package confusione.launcher.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Optional OptiFine. Its license forbids redistribution, so Confu Client never ships the
 * jar: when the user turns it on, the launcher fetches the official build from optifine.net
 * on the user's own machine, pins it by SHA-256, and puts it on the game classpath. The
 * client's tweaker registers OptiFine's class transformer (see GlideTweaker).
 */
public final class OptiFine {

	public static final String NAME = "OptiFine_1.8.9_HD_U_M5.jar";
	/** SHA-256 of the official OptiFine_1.8.9_HD_U_M5.jar (build 20210124-163719). */
	public static final String SHA256 = "2739760a70fca6ae5c7dc817bcc6283f18f55e38fa23778da7f387a6b724b2c4";

	static {
		// Older Java 8 builds may still offer TLS 1.0 first; optifine.net needs TLS 1.2+.
		if (System.getProperty("https.protocols") == null) {
			System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
		}
	}

	private OptiFine() {}

	public static File jar(File gameDir) {
		return new File(new File(gameDir, "optifine"), NAME);
	}

	/** The verified jar, or null when it is not there (or does not match the pin). */
	public static File installed(File gameDir) {
		File f = jar(gameDir);
		try {
			return f.isFile() && SHA256.equals(sha256(f)) ? f : null;
		} catch (IOException e) {
			return null;
		}
	}

	/** Official page the user can open to download the jar by hand. */
	public static final String PAGE_URL = "https://optifine.net/adloadx?f=" + NAME;
	private static final Pattern LINK_ANY = Pattern.compile("downloadx\\?f=" + Pattern.quote(NAME) + "(?:&amp;|&)x=([0-9a-fA-F]+)");
	private static final String BROWSER_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36 ConfuClientLauncher";

	/**
	 * Finds the pinned jar: the game-dir cache first, then a copy the user already downloaded
	 * (Downloads folder, home, the game dir itself); otherwise downloads it from optifine.net
	 * with retries. Throws with a user-readable message when everything fails.
	 */
	public static File ensure(File gameDir, Progress p) throws IOException {
		File have = installed(gameDir);
		if (have != null) {
			return have;
		}
		String home = System.getProperty("user.home", ".");
		File[] candidates = {
			new File(new File(home, "Downloads"), NAME), new File(home, NAME), new File(gameDir, NAME),
			new File(new File(gameDir, "mods"), NAME)
		};
		for (File c : candidates) {
			if (c.isFile()) {
				try {
					File got = importJar(c, gameDir);
					p.log("OptiFine: using " + c + " (SHA-256 verified)");
					return got;
				} catch (IOException e) {
					p.log("OptiFine: ignoring " + c + ": " + e.getMessage());
				}
			}
		}
		p.stage("Downloading OptiFine from optifine.net");
		IOException last = null;
		for (int attempt = 1; attempt <= 3; attempt++) {
			try {
				return download(gameDir, p);
			} catch (IOException e) {
				last = e;
				p.log("OptiFine download attempt " + attempt + "/3 failed: " + e);
				try {
					Thread.sleep(1500L * attempt);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
		throw new IOException("Could not download " + NAME + " from optifine.net (" + (last == null ? "interrupted" : last.getMessage()) + ")", last);
	}

	/** Copies a user-provided jar into the cache after checking it is the pinned build. */
	public static File importJar(File src, File gameDir) throws IOException {
		String got = sha256(src);
		if (!SHA256.equals(got)) {
			throw new IOException("not the official " + NAME + " (SHA-256 " + got + ")");
		}
		File target = jar(gameDir);
		if (src.getCanonicalFile().equals(target.getCanonicalFile())) {
			return target;
		}
		File dir = target.getParentFile();
		if (!dir.isDirectory() && !dir.mkdirs()) {
			throw new IOException("Cannot create " + dir);
		}
		File tmp = File.createTempFile("optifine", ".part", dir);
		try {
			try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(tmp)) {
				copy(in, out);
			}
			moveInto(tmp, target);
			return target;
		} finally {
			tmp.delete();
		}
	}

	private static File download(File gameDir, Progress p) throws IOException {
		String page = read(open(PAGE_URL, null));
		Matcher m = LINK_ANY.matcher(page);
		if (!m.find()) {
			throw new IOException("optifine.net download page changed: no link for " + NAME);
		}
		String url = "https://optifine.net/downloadx?f=" + NAME + "&x=" + m.group(1);
		File target = jar(gameDir);
		File dir = target.getParentFile();
		if (!dir.isDirectory() && !dir.mkdirs()) {
			throw new IOException("Cannot create " + dir);
		}
		File tmp = File.createTempFile("optifine", ".part", dir);
		try {
			HttpURLConnection c = open(url, PAGE_URL);
			String type = c.getContentType();
			if (type != null && type.toLowerCase().startsWith("text/html")) {
				throw new IOException("optifine.net returned a web page instead of the jar");
			}
			try (InputStream in = c.getInputStream(); OutputStream out = new FileOutputStream(tmp)) {
				copy(in, out);
			}
			String got = sha256(tmp);
			if (!SHA256.equals(got)) {
				throw new IOException("OptiFine SHA-256 mismatch: got " + got);
			}
			moveInto(tmp, target);
			p.log("OptiFine " + NAME + " verified (SHA-256 " + SHA256 + ")");
			return target;
		} finally {
			tmp.delete();
		}
	}

	private static void moveInto(File tmp, File target) throws IOException {
		if (target.exists() && !target.delete()) {
			throw new IOException("Cannot replace " + target);
		}
		if (!tmp.renameTo(target)) {
			throw new IOException("Cannot move OptiFine into " + target);
		}
	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[65536];
		int n;
		while ((n = in.read(buf)) > 0) {
			out.write(buf, 0, n);
		}
	}

	/** GET with a browser user agent; follows up to 5 redirects, including http/https switches. */
	private static HttpURLConnection open(String url, String referer) throws IOException {
		String u = url;
		for (int hop = 0; hop < 6; hop++) {
			HttpURLConnection c = (HttpURLConnection) new URL(u).openConnection();
			c.setInstanceFollowRedirects(false);
			c.setConnectTimeout(20000);
			c.setReadTimeout(60000);
			c.setRequestProperty("User-Agent", BROWSER_UA);
			c.setRequestProperty("Accept", "*/*");
			if (referer != null) {
				c.setRequestProperty("Referer", referer);
			}
			int code = c.getResponseCode();
			if (code >= 300 && code < 400 && c.getHeaderField("Location") != null) {
				u = new URL(new URL(u), c.getHeaderField("Location")).toString();
				c.disconnect();
				continue;
			}
			if (code != 200) {
				throw new IOException("HTTP " + code + " from " + u);
			}
			return c;
		}
		throw new IOException("Too many redirects from " + url);
	}

	private static String read(HttpURLConnection c) throws IOException {
		try (InputStream in = c.getInputStream()) {
			java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int n;
			while ((n = in.read(buf)) > 0) {
				b.write(buf, 0, n);
			}
			return new String(b.toByteArray(), "UTF-8");
		}
	}

	public static String sha256(File f) throws IOException {
		try (InputStream in = new FileInputStream(f)) {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] buf = new byte[65536];
			int n;
			while ((n = in.read(buf)) > 0) {
				md.update(buf, 0, n);
			}
			StringBuilder sb = new StringBuilder();
			for (byte x : md.digest()) {
				sb.append(String.format("%02x", x));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}
}
