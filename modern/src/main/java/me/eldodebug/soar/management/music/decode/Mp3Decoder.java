package me.eldodebug.soar.management.music.decode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

/** MP3 via JLayer (pure Java). Frames are decoded lazily into a small carry buffer. */
final class Mp3Decoder implements AudioDecoder {

	private final Bitstream bitstream;
	private final javazoom.jl.decoder.Decoder decoder = new javazoom.jl.decoder.Decoder();
	private final AudioFormat format;
	private final long durationMs;
	private byte[] carry = new byte[0];
	private int carryPos;
	private Header pending;

	Mp3Decoder(File f) throws IOException {
		bitstream = new Bitstream(new BufferedInputStream(new FileInputStream(f), 65536));
		try {
			pending = bitstream.readFrame();
		} catch (BitstreamException e) {
			throw new IOException("Bad MP3: " + f.getName(), e);
		}
		if (pending == null) {
			throw new IOException("Empty MP3: " + f.getName());
		}
		int channels = pending.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
		format = AudioDecoder.pcm16(pending.frequency(), channels);
		durationMs = scanDurationMs(f);
	}

	public AudioFormat getFormat() {
		return format;
	}

	public int read(byte[] buf) throws IOException {
		int written = 0;
		while (written < buf.length) {
			if (carryPos >= carry.length && !decodeNext()) {
				break;
			}
			int n = Math.min(buf.length - written, carry.length - carryPos);
			System.arraycopy(carry, carryPos, buf, written, n);
			carryPos += n;
			written += n;
		}
		return written == 0 ? -1 : written;
	}

	private boolean decodeNext() throws IOException {
		try {
			Header h = pending != null ? pending : bitstream.readFrame();
			pending = null;
			if (h == null) {
				return false;
			}
			SampleBuffer out = (SampleBuffer) decoder.decodeFrame(h, bitstream);
			bitstream.closeFrame();
			short[] s = out.getBuffer();
			int len = out.getBufferLength();
			if (carry.length != len * 2) {
				carry = new byte[len * 2];
			}
			for (int i = 0; i < len; i++) {
				carry[i * 2] = (byte) s[i];
				carry[i * 2 + 1] = (byte) (s[i] >> 8);
			}
			carryPos = 0;
			return true;
		} catch (BitstreamException e) {
			throw new IOException(e);
		} catch (DecoderException e) {
			throw new IOException(e);
		}
	}

	public long getDurationMs() {
		return durationMs;
	}

	public void close() throws IOException {
		try {
			bitstream.close();
		} catch (BitstreamException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Header-only pass over the file (no decoding, ~1 ms per MB). JLayer's
	 * Header.total_ms() trusts the first frame, which on LAME/ffmpeg files is the
	 * low-bitrate Xing/Info frame, and comes out about 2x too long.
	 */
	private static long scanDurationMs(File f) {
		Bitstream bs = null;
		try {
			bs = new Bitstream(new java.io.BufferedInputStream(new java.io.FileInputStream(f), 1 << 16));
			double ms = 0;
			Header h;
			while ((h = bs.readFrame()) != null) {
				ms += h.ms_per_frame();
				bs.closeFrame();
			}
			return ms > 0 ? (long) ms : -1;
		} catch (Exception e) {
			return -1;
		} finally {
			if (bs != null) {
				try {
					bs.close();
				} catch (Exception ignored) {
				}
			}
		}
	}
}
