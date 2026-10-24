package me.eldodebug.soar.management.music.decode;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.sound.sampled.AudioFormat;

/**
 * Streams a file as signed 16-bit little-endian PCM. All implementations are pure
 * Java: javax.sound (WAV/AIFF/AU), JLayer (MP3), JOrbis (OGG Vorbis).
 */
public interface AudioDecoder extends Closeable {

	AudioFormat getFormat();

	/** Fills buf with PCM; returns bytes written, or -1 at end of stream. */
	int read(byte[] buf) throws IOException;

	/** Duration in ms if known, else -1. */
	long getDurationMs();

	static boolean isSupported(File f) {
		String n = f.getName().toLowerCase(Locale.ROOT);
		return n.endsWith(".mp3") || n.endsWith(".ogg") || n.endsWith(".wav") || n.endsWith(".aiff") || n.endsWith(".aif") || n.endsWith(".au");
	}

	static AudioDecoder open(File f) throws IOException {
		String n = f.getName().toLowerCase(Locale.ROOT);
		if (n.endsWith(".mp3")) {
			return new Mp3Decoder(f);
		}
		if (n.endsWith(".ogg")) {
			return new OggDecoder(f);
		}
		return new SampledDecoder(f);
	}

	static AudioFormat pcm16(float rate, int channels) {
		return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, channels, channels * 2, rate, false);
	}
}
