package me.eldodebug.soar.management.music;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.notification.NotificationType;

/**
 * Turns one YouTube or SoundCloud URL into an MP3 in the music folder.
 * The worker thread owns the process; the HUD only reads {@link #message()} and {@link #progress()}.
 */
public final class LinkGrabber {

	private static volatile boolean running;
	private static volatile float progress = -1f;
	private static volatile String message = "";

	private LinkGrabber() {}

	public static boolean active() {
		return running;
	}

	/** 0..1, or negative when the percent is not known yet. */
	public static float progress() {
		return progress;
	}

	public static String message() {
		return message;
	}

	public static boolean supported(String url) {
		if (url == null) {
			return false;
		}
		String u = url.trim().toLowerCase(Locale.ROOT);
		return u.startsWith("https://") || u.startsWith("http://");
	}

	public static boolean hostOk(String url) {
		try {
			String host = new java.net.URL(url.trim()).getHost().toLowerCase(Locale.ROOT);
			return host.equals("youtu.be") || host.endsWith(".youtube.com") || host.equals("youtube.com")
					|| host.equals("youtube-nocookie.com") || host.endsWith(".youtube-nocookie.com")
					|| host.equals("soundcloud.com") || host.endsWith(".soundcloud.com");
		} catch (Exception e) {
			return false;
		}
	}

	public static synchronized void start(final String url) {
		if (running) {
			return;
		}
		final String link = url == null ? "" : url.trim();
		if (!supported(link) || !hostOk(link)) {
			message = "Paste a YouTube or SoundCloud link";
			Glide.getInstance().getMusicManager().toastPublic(message, NotificationType.WARNING);
			return;
		}
		running = true;
		progress = -1f;
		message = "Starting download";
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					fetch(link);
				} catch (Exception e) {
					message = e.getMessage() == null ? "Download failed" : e.getMessage();
					Glide.getInstance().getMusicManager().toastPublic(message, NotificationType.ERROR);
				} finally {
					running = false;
				}
			}
		}, "CONFUSION3H-music-link");
		t.setDaemon(true);
		t.start();
	}

	/** Percent from a yt-dlp stderr line, or null. */
	static Float parsePercent(String line) {
		if (line == null || !line.contains("[download]") || !line.contains("%")) {
			return null;
		}
		int pct = line.indexOf('%');
		int s = pct;
		while (s > 0) {
			char c = line.charAt(s - 1);
			if ((c >= '0' && c <= '9') || c == '.') {
				s--;
			} else {
				break;
			}
		}
		if (s == pct) {
			return null;
		}
		try {
			return Float.valueOf(line.substring(s, pct));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static void fetch(String url) throws Exception {
		File music = Glide.getInstance().getFileManager().getMusicDir();
		if (!music.isDirectory() && !music.mkdirs()) {
			throw new java.io.IOException("Cannot create " + music);
		}
		File tools = new File(music.getParentFile(), "tools");
		message = "Fetching yt-dlp and FFmpeg if needed";
		File ytdlp = ExternalTools.ytDlp(tools);
		File ffmpeg = ExternalTools.ffmpeg(tools);
		long started = System.currentTimeMillis();
		message = "Downloading";
		ProcessBuilder pb = new ProcessBuilder(
				ytdlp.getAbsolutePath(),
				"--no-playlist",
				"--no-mtime",
				"-x",
				"--audio-format", "mp3",
				"--ffmpeg-location", ffmpeg.getParentFile().getAbsolutePath(),
				"-o", new File(music, "%(title).150B.%(ext)s").getAbsolutePath(),
				url);
		pb.redirectErrorStream(true);
		Process proc = pb.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));
		String line;
		StringBuilder tail = new StringBuilder();
		while ((line = r.readLine()) != null) {
			Float pct = parsePercent(line);
			if (pct != null) {
				progress = Math.max(0f, Math.min(1f, pct.floatValue() / 100f));
				message = "Downloading " + Math.round(pct.floatValue()) + "%";
			}
			if (tail.length() < 2000) {
				tail.append(line).append('\n');
			}
		}
		int code = proc.waitFor();
		if (code != 0) {
			throw new java.io.IOException("yt-dlp exited " + code);
		}
		File newest = newestAudio(music, started - 2000);
		progress = 1f;
		message = newest == null ? "Saved" : "Saved " + newest.getName();
		if (newest != null) {
			Glide.getInstance().getMusicManager().playDownloaded(newest);
		}
		Glide.getInstance().getMusicManager().toastPublic(message, NotificationType.SUCCESS);
	}

	private static File newestAudio(File dir, long since) {
		File[] files = dir.listFiles();
		File best = null;
		if (files == null) {
			return null;
		}
		for (File f : files) {
			String n = f.getName().toLowerCase(Locale.ROOT);
			if (!f.isFile() || f.lastModified() < since) {
				continue;
			}
			if (!(n.endsWith(".mp3") || n.endsWith(".ogg") || n.endsWith(".wav"))) {
				continue;
			}
			if (best == null || f.lastModified() > best.lastModified()) {
				best = f;
			}
		}
		return best;
	}
}
