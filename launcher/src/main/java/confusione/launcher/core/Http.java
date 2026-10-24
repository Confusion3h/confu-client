package confusione.launcher.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** HTTPS downloads with SHA-1 verification, retries and atomic writes. */
public final class Http {

	private Http() {}

	public static String getText(String url) throws IOException {
		InputStream in = open(url);
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			copy(in, out, null);
			return new String(out.toByteArray(), StandardCharsets.UTF_8);
		} finally {
			in.close();
		}
	}

	/**
	 * Downloads url to target unless it already exists with the expected sha1 (or, when
	 * sha1 is null, simply exists). Returns bytes downloaded (0 when cached).
	 */
	public static long download(String url, File target, String sha1, long[] counter) throws IOException {
		if (target.isFile() && (sha1 == null || sha1.equalsIgnoreCase(sha1(target)))) {
			return 0;
		}
		IOException last = null;
		for (int attempt = 0; attempt < 3; attempt++) {
			try {
				return tryDownload(url, target, sha1, counter);
			} catch (IOException e) {
				last = e;
			}
		}
		throw last;
	}

	private static long tryDownload(String url, File target, String sha1, long[] counter) throws IOException {
		File parent = target.getParentFile();
		if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
			throw new IOException("Cannot create " + parent);
		}
		File tmp = new File(target.getPath() + ".part");
		long n;
		InputStream in = open(url);
		try {
			OutputStream out = new FileOutputStream(tmp);
			try {
				n = copy(in, out, counter);
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
		if (sha1 != null) {
			String got = sha1(tmp);
			if (!sha1.equalsIgnoreCase(got)) {
				tmp.delete();
				throw new IOException("SHA-1 mismatch for " + url + " (" + got + ")");
			}
		}
		if (target.exists() && !target.delete()) {
			throw new IOException("Cannot replace " + target);
		}
		if (!tmp.renameTo(target)) {
			throw new IOException("Cannot move " + tmp + " to " + target);
		}
		return n;
	}

	private static InputStream open(String url) throws IOException {
		HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
		c.setConnectTimeout(15000);
		c.setReadTimeout(30000);
		c.setRequestProperty("User-Agent", "CONFUSION3H-launcher");
		int code = c.getResponseCode();
		if (code != 200) {
			c.disconnect();
			throw new IOException("HTTP " + code + " for " + url);
		}
		return c.getInputStream();
	}

	private static long copy(InputStream in, OutputStream out, long[] counter) throws IOException {
		byte[] buf = new byte[16384];
		long total = 0;
		int r;
		while ((r = in.read(buf)) != -1) {
			out.write(buf, 0, r);
			total += r;
			if (counter != null) {
				synchronized (counter) {
					counter[0] += r;
				}
			}
		}
		return total;
	}

	public static String sha1(File f) throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			InputStream in = new FileInputStream(f);
			try {
				byte[] buf = new byte[16384];
				int r;
				while ((r = in.read(buf)) != -1) {
					md.update(buf, 0, r);
				}
			} finally {
				in.close();
			}
			StringBuilder b = new StringBuilder();
			for (byte x : md.digest()) {
				b.append(String.format("%02x", x));
			}
			return b.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}
}
