package me.eldodebug.soar.management.music;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import me.eldodebug.soar.management.music.decode.AudioDecoder;

/**
 * Plays files from a folder through javax.sound (pure Java, no natives beyond the
 * JDK's own). One daemon thread exists only while a track is loaded; when paused it
 * blocks on a monitor, so idle cost is zero. Volume is applied in software because
 * MASTER_GAIN is not available on every mixer.
 */
public final class LocalPlayer {

	public interface Listener {
		void changed(NowPlaying np);
		void error(String message);
	}

	private final Listener listener;
	private final List<File> playlist = new ArrayList<File>();
	private final Random random = new Random();
	private File folder;
	private int index = -1;
	private volatile float volume = 0.6f;
	private volatile boolean shuffle;
	private volatile boolean paused;
	private volatile boolean stopRequested;
	private volatile int skip; // +1 next, -1 previous, 0 none
	private volatile Thread thread;
	private boolean failed; // only touched by the playback thread
	private final Object lock = new Object();
	private volatile String title;
	private volatile long positionMs, durationMs = -1;

	public LocalPlayer(Listener listener) {
		this.listener = listener;
	}

	public synchronized int scan(File dir) {
		folder = dir;
		playlist.clear();
		File[] files = dir.listFiles();
		if (files != null) {
			Arrays.sort(files);
			for (File f : files) {
				if (f.isFile() && AudioDecoder.isSupported(f)) {
					playlist.add(f);
				}
			}
		}
		if (index >= playlist.size()) {
			index = -1;
		}
		return playlist.size();
	}

	public synchronized List<File> getPlaylist() {
		return Collections.unmodifiableList(new ArrayList<File>(playlist));
	}

	public File getFolder() {
		return folder;
	}

	public void setVolume(float v) {
		volume = Math.max(0, Math.min(1, v));
	}

	public void setShuffle(boolean s) {
		shuffle = s;
	}

	public boolean isActive() {
		Thread t = thread;
		return t != null && t.isAlive();
	}

	public boolean isPlaying() {
		return isActive() && !paused;
	}

	public void togglePause() {
		if (!isActive()) {
			play(index < 0 ? 0 : index);
			return;
		}
		synchronized (lock) {
			paused = !paused;
			lock.notifyAll();
		}
		publish();
	}

	public void next() {
		if (!isActive()) {
			play(nextIndex(1));
			return;
		}
		skip = 1;
		wake();
	}

	public void previous() {
		if (!isActive()) {
			play(nextIndex(-1));
			return;
		}
		// restart the track if we're more than 3s in, like most players
		skip = positionMs > 3000 ? 2 : -1;
		wake();
	}

	public void play(int i) {
		stop(); // outside the monitor: the old thread may need it to finish
		synchronized (this) {
			if (playlist.isEmpty()) {
				listener.error("No music files in " + folder);
				return;
			}
			index = Math.floorMod(i, playlist.size());
		}
		stopRequested = false;
		paused = false;
		thread = new Thread(new Runnable() {
			public void run() {
				loop();
			}
		}, "confusione-music");
		thread.setDaemon(true);
		thread.setPriority(Thread.NORM_PRIORITY - 1);
		thread.start();
	}

	public void stop() {
		Thread t = thread;
		if (t == null) {
			return;
		}
		stopRequested = true;
		wake();
		try {
			t.join(2000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		thread = null;
		title = null;
		publish();
	}

	private boolean stopped() {
		return stopRequested || Thread.currentThread() != thread;
	}

	private void wake() {
		synchronized (lock) {
			paused = false;
			lock.notifyAll();
		}
	}

	private synchronized int nextIndex(int dir) {
		if (playlist.isEmpty()) {
			return 0;
		}
		if (shuffle && dir > 0 && playlist.size() > 1) {
			int n;
			do {
				n = random.nextInt(playlist.size());
			} while (n == index);
			return n;
		}
		return Math.floorMod(index + dir, playlist.size());
	}

	private void loop() {
		int failures = 0;
		while (!stopped()) {
			File f;
			int size;
			synchronized (this) {
				if (playlist.isEmpty()) {
					break;
				}
				f = playlist.get(index);
				size = playlist.size();
			}
			failed = false;
			int result = playFile(f);
			failures = failed ? failures + 1 : 0;
			if (failures >= size) {
				break; // every file failed: don't spin
			}
			if (stopped()) {
				break;
			}
			int s = result != 0 ? result : 1; // natural end -> next
			if (s == 2) {
				continue; // restart same track
			}
			synchronized (this) {
				index = nextIndex(s);
			}
		}
		if (Thread.currentThread() == thread) {
			title = null;
			publish();
		}
	}

	/** @return 0 at end of track, else the skip code that interrupted it. */
	private int playFile(File f) {
		AudioDecoder dec = null;
		SourceDataLine line = null;
		try {
			dec = AudioDecoder.open(f);
			AudioFormat fmt = dec.getFormat();
			try {
				line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, fmt));
			} catch (IllegalArgumentException noDevice) {
				throw new LineUnavailableException(noDevice.getMessage()); // no sound output at all
			}
			line.open(fmt, (int) fmt.getFrameRate() * fmt.getFrameSize() / 4); // ~250 ms buffer
			line.start();
			title = displayName(f);
			durationMs = dec.getDurationMs();
			positionMs = 0;
			publish();

			byte[] buf = new byte[4096 - 4096 % fmt.getFrameSize()];
			long bytesPerSec = (long) fmt.getFrameRate() * fmt.getFrameSize();
			long played = 0;
			int n;
			while ((n = dec.read(buf)) > 0) {
				if (stopped()) {
					return 0;
				}
				if (skip != 0) {
					int s = skip;
					skip = 0;
					line.flush();
					return s;
				}
				if (paused) {
					line.stop();
					publish();
					synchronized (lock) {
						while (paused && !stopped() && skip == 0) {
							lock.wait();
						}
					}
					line.start();
					publish();
					if (stopped()) {
						return 0;
					}
					if (skip != 0) {
						int s = skip;
						skip = 0;
						return s;
					}
				}
				applyVolume(buf, n, volume);
				line.write(buf, 0, n);
				played += n;
				long pos = played * 1000 / bytesPerSec;
				if (pos / 1000 != positionMs / 1000) {
					positionMs = pos;
					publish(); // once per second, not per buffer
				}
			}
			line.drain();
			return 0;
		} catch (LineUnavailableException e) {
			listener.error("No audio output available: " + e.getMessage());
			stopRequested = true;
			return 0;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			stopRequested = true;
			return 0;
		} catch (Exception e) {
			listener.error("Cannot play " + f.getName() + ": " + e.getMessage());
			failed = true;
			return 1;
		} finally {
			if (line != null) {
				line.close();
			}
			if (dec != null) {
				try {
					dec.close();
				} catch (Exception ignored) {
				}
			}
		}
	}

	static void applyVolume(byte[] buf, int n, float vol) {
		if (vol >= 0.999f) {
			return;
		}
		// perceptual curve: slider 0.5 ~= -12 dB
		float g = vol * vol;
		for (int i = 0; i + 1 < n; i += 2) {
			int s = (short) ((buf[i] & 0xFF) | (buf[i + 1] << 8));
			s = (int) (s * g);
			buf[i] = (byte) s;
			buf[i + 1] = (byte) (s >> 8);
		}
	}

	static String displayName(File f) {
		String n = f.getName();
		int dot = n.lastIndexOf('.');
		return dot > 0 ? n.substring(0, dot) : n;
	}

	private void publish() {
		String t = title;
		if (t == null) {
			listener.changed(NowPlaying.NOTHING);
			return;
		}
		String artist = null, name = t;
		int dash = t.indexOf(" - ");
		if (dash > 0) {
			artist = t.substring(0, dash);
			name = t.substring(dash + 3);
		}
		listener.changed(new NowPlaying(name, artist, positionMs, durationMs, !paused, System.currentTimeMillis()));
	}
}
