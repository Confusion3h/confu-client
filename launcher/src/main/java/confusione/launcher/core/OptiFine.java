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
	private static final String PAGE = "https://optifine.net/adloadx?f=" + NAME;
	private static final Pattern LINK = Pattern.compile("downloadx\\?f=" + Pattern.quote(NAME) + "&x=[0-9a-fA-F]+");
	private static final String UA = "Mozilla/5.0 (Confu Client launcher)";

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

	/** Downloads the pinned jar from optifine.net if needed. Throws on any mismatch. */
	public static File ensure(File gameDir, Progress p) throws IOException {
		File have = installed(gameDir);
		if (have != null) {
			return have;
		}
		p.stage("Downloading OptiFine from optifine.net");
		String page = read(open(PAGE, null));
		Matcher m = LINK.matcher(page);
		if (!m.find()) {
			throw new IOException("optifine.net download page changed: no link for " + NAME);
		}
		String url = "https://optifine.net/" + m.group();
		File target = jar(gameDir);
		File dir = target.getParentFile();
		if (!dir.isDirectory() && !dir.mkdirs()) {
			throw new IOException("Cannot create " + dir);
		}
		File tmp = File.createTempFile("optifine", ".part", dir);
		try {
			HttpURLConnection c = open(url, PAGE);
			try (InputStream in = c.getInputStream(); OutputStream out = new FileOutputStream(tmp)) {
				byte[] buf = new byte[65536];
				int n;
				while ((n = in.read(buf)) > 0) {
					out.write(buf, 0, n);
				}
			}
			String got = sha256(tmp);
			if (!SHA256.equals(got)) {
				throw new IOException("OptiFine SHA-256 mismatch: got " + got);
			}
			if (target.exists() && !target.delete()) {
				throw new IOException("Cannot replace " + target);
			}
			if (!tmp.renameTo(target)) {
				throw new IOException("Cannot move OptiFine into " + target);
			}
			p.log("OptiFine " + NAME + " verified (SHA-256 " + SHA256 + ")");
			return target;
		} finally {
			tmp.delete();
		}
	}

	private static HttpURLConnection open(String url, String referer) throws IOException {
		HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
		c.setConnectTimeout(15000);
		c.setReadTimeout(30000);
		c.setRequestProperty("User-Agent", UA);
		if (referer != null) {
			c.setRequestProperty("Referer", referer);
		}
		int code = c.getResponseCode();
		if (code != 200) {
			throw new IOException("HTTP " + code + " from " + url);
		}
		return c;
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

	static String sha256(File f) throws IOException {
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
