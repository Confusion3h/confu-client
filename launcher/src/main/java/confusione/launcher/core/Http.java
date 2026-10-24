package confusione.launcher.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

/**
 * HTTPS downloads with SHA-1 verification, retries and atomic writes.
 * Each attempt writes a unique temp file in the destination directory: the 1.8 asset
 * index lists the same hash twice, and eight workers used to share one {@code .part}.
 */
public final class Http {

	private static final ConcurrentHashMap<String, Object> LOCKS = new ConcurrentHashMap<String, Object>();
	private static final int ATTEMPTS = 4;

	private Http() {}

	public static final class StatusException extends IOException {
		public final int code;
		public final String body;

		public StatusException(int code, String body, String url) {
			super("HTTP " + code + " for " + hostPath(url) + suffix(body));
			this.code = code;
			this.body = body == null ? "" : body;
		}

		private static String hostPath(String url) {
			try {
				URL u = new URL(url);
				return u.getProtocol() + "://" + u.getHost() + u.getPath();
			} catch (Exception e) {
				return url;
			}
		}

		private static String suffix(String body) {
			if (body == null || body.isEmpty()) {
				return "";
			}
			String s = body.replace('\n', ' ').replace('\r', ' ');
			if (s.length() > 240) {
				s = s.substring(0, 240);
			}
			return ": " + s;
		}
	}

	public static String getText(String url) throws IOException {
		return get(url, null);
	}

	public static String get(String url, String authorization) throws IOException {
		HttpURLConnection c = connect(url);
		c.setRequestMethod("GET");
		if (authorization != null) {
			c.setRequestProperty("Authorization", authorization);
		}
		return readResponse(c, url);
	}

	public static String post(String url, String contentType, String body, String authorization) throws IOException {
		HttpURLConnection c = connect(url);
		c.setRequestMethod("POST");
		c.setDoOutput(true);
		c.setInstanceFollowRedirects(false);
		c.setRequestProperty("Content-Type", contentType);
		c.setRequestProperty("Accept", "application/json");
		if (authorization != null) {
			c.setRequestProperty("Authorization", authorization);
		}
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		OutputStream out = c.getOutputStream();
		try {
			out.write(bytes);
		} finally {
			out.close();
		}
		return readResponse(c, url);
	}

	/**
	 * Downloads url to target unless it already exists with the expected sha1 (or, when
	 * sha1 is null, simply exists). Returns bytes downloaded (0 when cached).
	 * Concurrent callers for the same path are serialized. A mismatch is retried;
	 * the last failure is thrown for the caller to report or abort.
	 */
	public static long download(String url, File target, String sha1, long[] counter) throws IOException {
		String key = target.getAbsolutePath();
		Object lock = LOCKS.get(key);
		if (lock == null) {
			Object created = new Object();
			Object prev = LOCKS.putIfAbsent(key, created);
			lock = prev == null ? created : prev;
		}
		synchronized (lock) {
			if (target.isFile() && (sha1 == null || sha1.equalsIgnoreCase(sha1(target)))) {
				return 0;
			}
			IOException last = null;
			for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
				try {
					return tryDownload(url, target, sha1, counter);
				} catch (IOException e) {
					last = e;
					if (attempt + 1 < ATTEMPTS) {
						try {
							Thread.sleep(200L * (attempt + 1));
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
							throw new IOException("Interrupted", ie);
						}
					}
				}
			}
			throw last;
		}
	}

	private static long tryDownload(String url, File target, String sha1, long[] counter) throws IOException {
		File parent = target.getParentFile();
		if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
			throw new IOException("Cannot create " + parent);
		}
		File tmp = File.createTempFile("dl-", ".part", parent);
		long n;
		try {
			HttpURLConnection c = connect(url);
			int code = c.getResponseCode();
			if (code != 200) {
				String err = readFully(c.getErrorStream());
				c.disconnect();
				throw new StatusException(code, err, url);
			}
			InputStream in = c.getInputStream();
			String enc = c.getContentEncoding();
			if (enc != null && enc.toLowerCase().contains("gzip")) {
				in = new GZIPInputStream(in);
			}
			try {
				OutputStream out = new java.io.FileOutputStream(tmp);
				try {
					n = copy(in, out, counter);
				} finally {
					out.close();
				}
			} finally {
				in.close();
				c.disconnect();
			}
			if (sha1 != null) {
				String got = sha1(tmp);
				if (!sha1.equalsIgnoreCase(got)) {
					throw new IOException("SHA-1 mismatch for " + url + " (" + got + ")");
				}
			}
			moveAtomically(tmp, target);
			return n;
		} finally {
			if (tmp.exists()) {
				tmp.delete();
			}
		}
	}

	private static void moveAtomically(File tmp, File target) throws IOException {
		try {
			Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			return;
		} catch (AtomicMoveNotSupportedException e) {
			// fall through
		} catch (IOException e) {
			// Windows often rejects ATOMIC_MOVE when the destination exists
		}
		Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	private static HttpURLConnection connect(String url) throws IOException {
		HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
		c.setConnectTimeout(15000);
		c.setReadTimeout(30000);
		c.setUseCaches(false);
		c.setRequestProperty("User-Agent", "CONFUSION3H-launcher");
		c.setRequestProperty("Accept-Encoding", "identity");
		c.setRequestProperty("Connection", "close");
		return c;
	}

	private static String readResponse(HttpURLConnection c, String url) throws IOException {
		int code;
		try {
			code = c.getResponseCode();
		} catch (IOException e) {
			c.disconnect();
			throw e;
		}
		InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
		String text = readFully(in);
		c.disconnect();
		if (code < 200 || code >= 300) {
			throw new StatusException(code, text, url);
		}
		return text;
	}

	private static String readFully(InputStream in) throws IOException {
		if (in == null) {
			return "";
		}
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			copy(in, out, null);
			return new String(out.toByteArray(), StandardCharsets.UTF_8);
		} finally {
			in.close();
		}
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
