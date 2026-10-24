package me.eldodebug.soar.management.music;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * yt-dlp and FFmpeg for turning a YouTube or SoundCloud link into an MP3.
 * Looks beside the game, then downloads a pinned build into glide/tools and checks SHA-256.
 */
public final class ExternalTools {

	private static final String YTDLP_URL = "https://github.com/yt-dlp/yt-dlp/releases/download/2026.10.21/yt-dlp.exe";
	private static final String YTDLP_SHA = "33121d192d706e62f684a06f80918e3f3a8ba52ac3a65213b6951c39c7d9f555";
	private static final String FFMPEG_URL = "https://github.com/yt-dlp/FFmpeg-Builds/releases/download/autobuild-2026-10-21-18-38/ffmpeg-N-127911-gadfe258a6a-win64-gpl.zip";
	private static final String FFMPEG_SHA = "00d61fec4d25bacdd064f468ca731a20ccce35b49e1fedaac807374f2d705ed1";

	private ExternalTools() {}

	public static File ytDlp(File toolsDir) throws IOException {
		return tool(toolsDir, windows() ? "yt-dlp.exe" : "yt-dlp", YTDLP_URL, YTDLP_SHA, false);
	}

	public static File ffmpeg(File toolsDir) throws IOException {
		return tool(toolsDir, windows() ? "ffmpeg.exe" : "ffmpeg", FFMPEG_URL, FFMPEG_SHA, true);
	}

	private static boolean windows() {
		return System.getProperty("os.name", "").toLowerCase().contains("win");
	}

	private static File tool(File toolsDir, String name, String url, String sha, boolean zip) throws IOException {
		File bundled = bundled(name);
		if (bundled != null) {
			return bundled;
		}
		if (!windows()) {
			File path = onPath(name);
			if (path != null) {
				return path;
			}
			throw new IOException(name + " is not installed. On Windows the client downloads it; on this system install " + name + " and leave it on PATH.");
		}
		if (!toolsDir.isDirectory() && !toolsDir.mkdirs()) {
			throw new IOException("Cannot create " + toolsDir);
		}
		File dest = new File(toolsDir, name);
		if (dest.isFile()) {
			return dest;
		}
		File tmp = new File(toolsDir, name + ".download");
		download(url, tmp, sha);
		if (zip) {
			extractExe(tmp, dest, "ffmpeg.exe");
			tmp.delete();
		} else if (!tmp.renameTo(dest)) {
			throw new IOException("Cannot save " + dest);
		}
		return dest;
	}

	private static File bundled(String name) {
		String env = System.getenv("CONFUSION3H_TOOLS");
		if (env != null && !env.isEmpty()) {
			File f = new File(env, name);
			if (f.isFile()) {
				return f;
			}
		}
		return null;
	}

	private static File onPath(String name) {
		String path = System.getenv("PATH");
		if (path == null) {
			return null;
		}
		for (String dir : path.split(File.pathSeparator)) {
			File f = new File(dir, name);
			if (f.isFile()) {
				return f;
			}
		}
		return null;
	}

	private static void download(String url, File dest, String sha) throws IOException {
		HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
		c.setConnectTimeout(20000);
		c.setReadTimeout(120000);
		c.setInstanceFollowRedirects(true);
		c.setRequestProperty("User-Agent", "CONFUSION3H");
		int code = c.getResponseCode();
		if (code != 200) {
			c.disconnect();
			throw new IOException("HTTP " + code + " downloading " + url);
		}
		InputStream in = c.getInputStream();
		try {
			FileOutputStream out = new FileOutputStream(dest);
			try {
				byte[] buf = new byte[65536];
				int n;
				while ((n = in.read(buf)) != -1) {
					out.write(buf, 0, n);
				}
			} finally {
				out.close();
			}
		} finally {
			in.close();
			c.disconnect();
		}
		String got = sha256(dest);
		if (!sha.equalsIgnoreCase(got)) {
			dest.delete();
			throw new IOException("Checksum mismatch for " + dest.getName());
		}
	}

	private static void extractExe(File zip, File dest, String exeName) throws IOException {
		ZipInputStream zin = new ZipInputStream(new java.io.FileInputStream(zip));
		try {
			ZipEntry e;
			while ((e = zin.getNextEntry()) != null) {
				if (e.isDirectory() || !e.getName().replace('\\', '/').endsWith("/bin/" + exeName)) {
					continue;
				}
				FileOutputStream out = new FileOutputStream(dest);
				try {
					byte[] buf = new byte[65536];
					int n;
					while ((n = zin.read(buf)) != -1) {
						out.write(buf, 0, n);
					}
				} finally {
					out.close();
				}
				return;
			}
		} finally {
			zin.close();
		}
		throw new IOException("ffmpeg.exe was not in the downloaded archive");
	}

	private static String sha256(File f) throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			InputStream in = new java.io.FileInputStream(f);
			try {
				byte[] buf = new byte[65536];
				int n;
				while ((n = in.read(buf)) != -1) {
					md.update(buf, 0, n);
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
