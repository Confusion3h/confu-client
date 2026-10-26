package me.eldodebug.soar.management.music;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * yt-dlp and FFmpeg for turning a YouTube or SoundCloud link into an MP3.
 * Downloads a pinned build into glide/tools and checks SHA-256. Every OS.
 */
public final class ExternalTools {

	private static final String YTDLP = "https://github.com/yt-dlp/yt-dlp/releases/download/2026.10.21/";
	private static final String FFMPEG = "https://github.com/yt-dlp/FFmpeg-Builds/releases/download/autobuild-2026-10-21-18-38/";
	private static final String FFMPEG_REV = "ffmpeg-N-127911-gadfe258a6a-";
	private static final String MAC_FFMPEG = "https://github.com/eugeneware/ffmpeg-static/releases/download/b6.1.1/";

	private ExternalTools() {}

	public static File ytDlp(File toolsDir) throws IOException {
		String name = windows() ? "yt-dlp.exe" : "yt-dlp";
		String asset;
		String sha;
		if (windows() && arm()) {
			asset = "yt-dlp_arm64.exe";
			sha = "1380a9f105302764e6a5d383c2f07238ae242f0f4fbf3414aea00e56619a79b3";
		} else if (windows()) {
			asset = "yt-dlp.exe";
			sha = "33121d192d706e62f684a06f80918e3f3a8ba52ac3a65213b6951c39c7d9f555";
		} else if (linux() && arm()) {
			asset = "yt-dlp_linux_aarch64";
			sha = "e137c2171b0b8ef648e6136e86aa70c65923e93e434f01c93901d64744b190aa";
		} else if (linux()) {
			asset = "yt-dlp_linux";
			sha = "d8bd33fca9d5b23bd5b9952b3fca36db5c46331933fda9f598a3da14ce2c243f";
		} else {
			asset = "yt-dlp_macos";
			sha = "7a6fe71d7d680f1fb0ac18d4949873d77db2859e90017c0f44bd94b23be4ee40";
		}
		return tool(toolsDir, name, YTDLP + asset, sha, Kind.FILE);
	}

	public static File ffmpeg(File toolsDir) throws IOException {
		String name = windows() ? "ffmpeg.exe" : "ffmpeg";
		if (mac()) {
			String asset = arm() ? "ffmpeg-darwin-arm64.gz" : "ffmpeg-darwin-x64.gz";
			String sha = arm() ? MacFfmpeg.ARM64 : MacFfmpeg.X64;
			return tool(toolsDir, name, MAC_FFMPEG + asset, sha, Kind.GZIP);
		}
		String os = windows() ? (arm() ? "winarm64" : "win64") : (arm() ? "linuxarm64" : "linux64");
		boolean zip = windows();
		String asset = FFMPEG_REV + os + "-gpl." + (zip ? "zip" : "tar.xz");
		String sha = ffmpegSha(os);
		return tool(toolsDir, name, FFMPEG + asset, sha, zip ? Kind.ZIP_EXE : Kind.TAR_XZ);
	}

	private static String ffmpegSha(String os) {
		if ("win64".equals(os)) {
			return "00d61fec4d25bacdd064f468ca731a20ccce35b49e1fedaac807374f2d705ed1";
		}
		if ("winarm64".equals(os)) {
			return "1d368a3f73562e962ed90984de5547031467e2ce5c89804ad860022f7a65d904";
		}
		if ("linuxarm64".equals(os)) {
			return "5a721be9d626e8aaae024889827380868709002e8a4cfad26abc7bcc8cff365f";
		}
		return "bf3d9ff8c9ff7300b9c31d44becb6a68c12e62af661cc75e3b12fa01f0a5d0d2";
	}

	private enum Kind { FILE, ZIP_EXE, TAR_XZ, GZIP }

	/** eugeneware/ffmpeg-static b6.1.1 gzip checksums. yt-dlp's autobuild has no macOS archive. */
	static final class MacFfmpeg {
		static final String X64 = "929b375c1182d956c51f7ac25e0b2b0411fb01f6f407aa15c9758efeb4242106";
		static final String ARM64 = "8923876afa8db5585022d7860ec7e589af192f441c56793971276d450ed3bbfa";
	}

	private static boolean windows() {
		return System.getProperty("os.name", "").toLowerCase().contains("win");
	}

	private static boolean mac() {
		String n = System.getProperty("os.name", "").toLowerCase();
		return n.contains("mac");
	}

	private static boolean linux() {
		return !windows() && !mac();
	}

	private static boolean arm() {
		String a = System.getProperty("os.arch", "").toLowerCase();
		return a.equals("aarch64") || a.equals("arm64");
	}

	private static File tool(File toolsDir, String name, String url, String sha, Kind kind) throws IOException {
		File bundled = bundled(name);
		if (bundled != null) {
			return bundled;
		}
		if (!toolsDir.isDirectory() && !toolsDir.mkdirs()) {
			throw new IOException("Cannot create " + toolsDir);
		}
		File dest = new File(toolsDir, name);
		if (dest.isFile() && dest.length() > 0) {
			return dest;
		}
		File tmp = new File(toolsDir, name + ".download");
		try {
			download(url, tmp, sha);
			if (kind == Kind.ZIP_EXE) {
				extractZipExe(tmp, dest, "ffmpeg.exe");
				tmp.delete();
			} else if (kind == Kind.TAR_XZ) {
				extractTarXz(tmp, dest, name);
				tmp.delete();
			} else if (kind == Kind.GZIP) {
				gunzip(tmp, dest);
				tmp.delete();
			} else if (!tmp.renameTo(dest)) {
				throw new IOException("Cannot save " + dest);
			}
			if (!windows()) {
				dest.setExecutable(true, false);
			}
			return dest;
		} catch (IOException e) {
			tmp.delete();
			File path = onPath(name);
			if (path != null) {
				return path;
			}
			throw e;
		}
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
		HttpURLConnection c = open(url);
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
			throw new IOException("Checksum mismatch for " + dest.getName() + " (got " + got + ")");
		}
	}

	/** GitHub answers 406 to the JDK's default Accept header. Follow the release redirect ourselves. */
	private static HttpURLConnection open(String url) throws IOException {
		String current = url;
		for (int hop = 0; hop < 5; hop++) {
			HttpURLConnection c = (HttpURLConnection) new URL(current).openConnection();
			c.setConnectTimeout(20000);
			c.setReadTimeout(180000);
			c.setInstanceFollowRedirects(false);
			c.setRequestProperty("User-Agent", "CONFUSION3H");
			c.setRequestProperty("Accept", "*/*");
			int code = c.getResponseCode();
			if (code >= 300 && code < 400) {
				String loc = c.getHeaderField("Location");
				c.disconnect();
				if (loc == null) {
					throw new IOException("HTTP " + code + " with no Location from " + current);
				}
				if (!loc.startsWith("http")) {
					loc = new URL(new URL(current), loc).toExternalForm();
				}
				current = loc;
				continue;
			}
			return c;
		}
		throw new IOException("Too many redirects downloading " + url);
	}

	private static void extractZipExe(File zip, File dest, String exeName) throws IOException {
		ZipInputStream zin = new ZipInputStream(new FileInputStream(zip));
		try {
			ZipEntry e;
			while ((e = zin.getNextEntry()) != null) {
				String n = e.getName().replace('\\', '/');
				if (e.isDirectory() || !(n.endsWith("/bin/" + exeName) || n.equals(exeName) || n.endsWith("/" + exeName))) {
					continue;
				}
				writeStream(zin, dest);
				return;
			}
		} finally {
			zin.close();
		}
		throw new IOException(exeName + " was not in the downloaded archive");
	}

	private static void extractTarXz(File archive, File dest, String exeName) throws IOException {
		File dir = new File(dest.getParentFile(), "ffmpeg-extract");
		if (dir.exists()) {
			deleteTree(dir);
		}
		dir.mkdirs();
		ProcessBuilder pb = new ProcessBuilder("tar", "-xJf", archive.getAbsolutePath(), "-C", dir.getAbsolutePath());
		pb.redirectErrorStream(true);
		try {
			Process p = pb.start();
			InputStream in = p.getInputStream();
			byte[] buf = new byte[4096];
			while (in.read(buf) != -1) {
			}
			in.close();
			int code = p.waitFor();
			if (code != 0) {
				throw new IOException("tar exited " + code);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("tar interrupted");
		}
		File found = findNamed(dir, exeName);
		if (found == null) {
			throw new IOException(exeName + " was not in the downloaded archive");
		}
		if (!found.renameTo(dest)) {
			copyFile(found, dest);
			found.delete();
		}
		deleteTree(dir);
	}

	private static File findNamed(File dir, String name) {
		File[] kids = dir.listFiles();
		if (kids == null) {
			return null;
		}
		for (File f : kids) {
			if (f.isDirectory()) {
				File inner = findNamed(f, name);
				if (inner != null) {
					return inner;
				}
			} else if (f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}

	private static void gunzip(File gz, File dest) throws IOException {
		GZIPInputStream in = new GZIPInputStream(new FileInputStream(gz));
		try {
			writeStream(in, dest);
		} finally {
			in.close();
		}
	}

	private static void writeStream(InputStream in, File dest) throws IOException {
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
	}

	private static void copyFile(File from, File to) throws IOException {
		FileInputStream in = new FileInputStream(from);
		try {
			writeStream(in, to);
		} finally {
			in.close();
		}
	}

	private static void deleteTree(File f) {
		if (f.isDirectory()) {
			File[] kids = f.listFiles();
			if (kids != null) {
				for (File k : kids) {
					deleteTree(k);
				}
			}
		}
		f.delete();
	}

	private static String sha256(File f) throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			InputStream in = new FileInputStream(f);
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
