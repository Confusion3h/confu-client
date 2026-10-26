package me.eldodebug.confu.music;

import me.eldodebug.confu.ConfuClient;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/** Plays the user's own .wav and .ogg files from .minecraft/confu/music. No tracks are bundled. */
public final class MusicPlayer {

	private final Path folder;
	private final List<Path> tracks = new ArrayList<>();
	private volatile int index = -1;
	private volatile boolean paused;
	private volatile float volume = 0.5F;
	private volatile String status = "";
	private Thread thread;
	private volatile boolean stop;

	public MusicPlayer(Path folder) {
		this.folder = folder;
	}

	public Path folder() {
		return folder;
	}

	public void scan(boolean shuffle) {
		tracks.clear();
		try {
			Files.createDirectories(folder);
			try (Stream<Path> files = Files.list(folder)) {
				files.filter(MusicPlayer::supported).sorted().forEach(tracks::add);
			}
		} catch (IOException e) {
			ConfuClient.LOGGER.warn("Could not read music folder {}", folder, e);
		}
		if (shuffle) {
			Collections.shuffle(tracks);
		}
	}

	private static boolean supported(Path path) {
		String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
		return name.endsWith(".wav") || name.endsWith(".ogg");
	}

	public int size() {
		return tracks.size();
	}

	public String current() {
		if (index < 0 || index >= tracks.size()) {
			return null;
		}
		String name = tracks.get(index).getFileName().toString();
		int dot = name.lastIndexOf('.');
		return dot > 0 ? name.substring(0, dot) : name;
	}

	public String status() {
		return status;
	}

	public boolean paused() {
		return paused;
	}

	public void setVolume(float volume) {
		this.volume = volume;
	}

	public void togglePause() {
		paused = !paused;
	}

	public void play(int next) {
		stopPlayback();
		if (tracks.isEmpty()) {
			index = -1;
			return;
		}
		index = Math.floorMod(next, tracks.size());
		paused = false;
		stop = false;
		Path track = tracks.get(index);
		thread = new Thread(() -> run(track), "Confu music");
		thread.setDaemon(true);
		thread.start();
	}

	public void next() {
		play(index + 1);
	}

	public void stopPlayback() {
		stop = true;
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
	}

	private void run(Path track) {
		try {
			AudioFormat format;
			byte[] pcm;
			if (track.toString().toLowerCase(Locale.ROOT).endsWith(".ogg")) {
				// Streamed decode into Java-owned buffers: nothing allocated by stb is freed by us.
				int channels;
				int rate;
				java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
				long handle;
				try (MemoryStack stack = MemoryStack.stackPush()) {
					IntBuffer error = stack.mallocInt(1);
					handle = STBVorbis.stb_vorbis_open_filename(track.toString(), error, null);
					if (handle == 0L) {
						status = "decode error";
						return;
					}
				}
				try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
					STBVorbis.stb_vorbis_get_info(handle, info);
					channels = info.channels();
					rate = info.sample_rate();
					ShortBuffer chunk = org.lwjgl.system.MemoryUtil.memAllocShort(4096 * channels);
					try {
						byte[] tmp = new byte[chunk.capacity() * 2];
						int frames;
						while ((frames = STBVorbis.stb_vorbis_get_samples_short_interleaved(handle, channels, chunk)) > 0 && !stop) {
							int n = frames * channels;
							for (int i = 0; i < n; i++) {
								short v = chunk.get(i);
								tmp[i * 2] = (byte) v;
								tmp[i * 2 + 1] = (byte) (v >> 8);
							}
							out.write(tmp, 0, n * 2);
						}
					} finally {
						org.lwjgl.system.MemoryUtil.memFree(chunk);
					}
				} finally {
					STBVorbis.stb_vorbis_close(handle);
				}
				pcm = out.toByteArray();
				format = new AudioFormat(rate, 16, channels, true, false);
			} else {
				try (AudioInputStream in = AudioSystem.getAudioInputStream(track.toFile())) {
					format = in.getFormat();
					pcm = in.readAllBytes();
				}
			}
			status = "";
			try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
				line.open(format);
				line.start();
				int frame = Math.max(1, format.getFrameSize());
				int chunk = frame * 2048;
				int pos = 0;
				while (pos < pcm.length && !stop) {
					if (paused) {
						Thread.sleep(50);
						continue;
					}
					applyVolume(line);
					int n = Math.min(chunk, pcm.length - pos);
					line.write(pcm, pos, n);
					pos += n;
				}
				if (!stop) {
					line.drain();
				}
			}
			if (!stop) {
				next();
			}
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			status = "no audio device";
			ConfuClient.LOGGER.warn("Music playback failed for {}: {}", track, e.toString());
		}
	}

	private void applyVolume(SourceDataLine line) {
		if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			FloatControl gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
			float db = volume <= 0.001F ? gain.getMinimum() : (float) (20.0 * Math.log10(volume));
			gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
		}
	}
}
