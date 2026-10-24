package me.eldodebug.soar.management.music.decode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

/**
 * OGG Vorbis via JOrbis, which Minecraft 1.8.9 already ships (paulscode codecjorbis),
 * so nothing extra is bundled. Single logical stream; chained streams end playback.
 */
final class OggDecoder implements AudioDecoder {

	private static final int CHUNK = 8192;

	private final InputStream in;
	private final SyncState sync = new SyncState();
	private final StreamState stream = new StreamState();
	private final Page page = new Page();
	private final Packet packet = new Packet();
	private final Info info = new Info();
	private final Comment comment = new Comment();
	private final DspState dsp = new DspState();
	private final Block block = new Block(dsp);
	private final AudioFormat format;
	private final float[][][] pcmHolder = new float[1][][];
	private final int[] index;
	private byte[] carry = new byte[0];
	private int carryLen, carryPos;
	private boolean eos;

	OggDecoder(File f) throws IOException {
		in = new BufferedInputStream(new FileInputStream(f), 65536);
		sync.init();
		if (!readPage()) {
			throw new IOException("Not an Ogg stream: " + f.getName());
		}
		stream.init(page.serialno());
		info.init();
		comment.init();
		if (stream.pagein(page) < 0 || stream.packetout(packet) != 1 || info.synthesis_headerin(comment, packet) < 0) {
			throw new IOException("Not Vorbis: " + f.getName());
		}
		int headers = 1;
		while (headers < 3) {
			int r = stream.packetout(packet);
			if (r == 1) {
				info.synthesis_headerin(comment, packet);
				headers++;
			} else if (!readPage() || stream.pagein(page) < 0) {
				throw new IOException("Truncated Vorbis header: " + f.getName());
			}
		}
		dsp.synthesis_init(info);
		block.init(dsp);
		index = new int[info.channels];
		format = AudioDecoder.pcm16(info.rate, info.channels);
	}

	/** Pulls bytes until one page is available. */
	private boolean readPage() throws IOException {
		while (true) {
			int r = sync.pageout(page);
			if (r == 1) {
				return true;
			}
			int off = sync.buffer(CHUNK);
			int n = in.read(sync.data, off, CHUNK);
			if (n <= 0) {
				return false;
			}
			sync.wrote(n);
		}
	}

	public AudioFormat getFormat() {
		return format;
	}

	public int read(byte[] buf) throws IOException {
		int written = 0;
		while (written < buf.length) {
			if (carryPos >= carryLen && !decodeMore()) {
				break;
			}
			int n = Math.min(buf.length - written, carryLen - carryPos);
			System.arraycopy(carry, carryPos, buf, written, n);
			carryPos += n;
			written += n;
		}
		return written == 0 ? -1 : written;
	}

	private boolean decodeMore() throws IOException {
		while (!eos) {
			int samples = dsp.synthesis_pcmout(pcmHolder, index);
			if (samples > 0) {
				int ch = info.channels;
				int need = samples * ch * 2;
				if (carry.length < need) {
					carry = new byte[need];
				}
				float[][] pcm = pcmHolder[0];
				for (int c = 0; c < ch; c++) {
					int pos = c * 2;
					for (int i = 0; i < samples; i++) {
						int v = (int) (pcm[c][index[c] + i] * 32767f);
						if (v > 32767) v = 32767;
						if (v < -32768) v = -32768;
						carry[pos] = (byte) v;
						carry[pos + 1] = (byte) (v >> 8);
						pos += ch * 2;
					}
				}
				dsp.synthesis_read(samples);
				carryLen = need;
				carryPos = 0;
				return true;
			}
			int r = stream.packetout(packet);
			if (r == 1) {
				if (block.synthesis(packet) == 0) {
					dsp.synthesis_blockin(block);
				}
				continue;
			}
			if (r == -1) {
				continue; // hole in data; skip
			}
			if (!readPage()) {
				eos = true;
				break;
			}
			stream.pagein(page);
			if (page.eos() != 0) {
				// drain what's left of this final page on the next loop iterations
				while (stream.packetout(packet) == 1) {
					if (block.synthesis(packet) == 0) {
						dsp.synthesis_blockin(block);
					}
				}
				int left = dsp.synthesis_pcmout(pcmHolder, index);
				if (left <= 0) {
					eos = true;
				}
			}
		}
		return false;
	}

	public long getDurationMs() {
		return -1; // would need a seek to the last granule; HUD shows elapsed only
	}

	public void close() throws IOException {
		stream.clear();
		block.clear();
		dsp.clear();
		info.clear();
		sync.clear();
		in.close();
	}
}
