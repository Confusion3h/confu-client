package me.eldodebug.soar.management.music.decode;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/** WAV / AIFF / AU through the JDK's javax.sound, converted to PCM16 LE. */
final class SampledDecoder implements AudioDecoder {

	private final AudioInputStream in;
	private final long durationMs;

	SampledDecoder(File f) throws IOException {
		try {
			AudioInputStream src = AudioSystem.getAudioInputStream(f);
			AudioFormat sf = src.getFormat();
			AudioFormat target = AudioDecoder.pcm16(sf.getSampleRate(), sf.getChannels());
			in = AudioSystem.getAudioInputStream(target, src);
			long frames = src.getFrameLength();
			durationMs = frames > 0 ? (long) (frames * 1000 / sf.getFrameRate()) : -1;
		} catch (UnsupportedAudioFileException e) {
			throw new IOException("Unsupported audio file: " + f.getName(), e);
		}
	}

	public AudioFormat getFormat() {
		return in.getFormat();
	}

	public int read(byte[] buf) throws IOException {
		int frame = in.getFormat().getFrameSize();
		return in.read(buf, 0, buf.length - buf.length % frame);
	}

	public long getDurationMs() {
		return durationMs;
	}

	public void close() throws IOException {
		in.close();
	}
}
