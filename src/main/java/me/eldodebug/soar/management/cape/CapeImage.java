package me.eldodebug.soar.management.cape;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import javax.imageio.ImageIO;

/**
 * Validation and normalisation of user cape images. Pure AWT, no Minecraft classes,
 * so it is unit-testable headless.
 *
 * Accepted (PNG only, checked by signature, not extension):
 * - full cape texture 64x32 and HD multiples (128x64, 256x128, ... up to 2048x1024)
 * - cape-only artwork 22x17 and multiples (44x34, 88x68, ...), the part players draw
 *   in cape editors; it is placed at the top-left of a 64x32 canvas, which is exactly
 *   where vanilla's cape model reads it.
 * Everything is returned as a 64:32 ARGB texture ready for DynamicTexture.
 */
public final class CapeImage {

	public static final int MAX_SCALE = 32; // 2048x1024
	private static final byte[] PNG_MAGIC = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n' };

	public enum Layout { FULL, CAPE_ONLY }

	public static final class Invalid extends Exception {
		private static final long serialVersionUID = 1L;
		public Invalid(String message) {
			super(message);
		}
	}

	public final BufferedImage texture; // 64k x 32k
	public final int scale;
	public final Layout source;

	private CapeImage(BufferedImage texture, int scale, Layout source) {
		this.texture = texture;
		this.scale = scale;
		this.source = source;
	}

	public static CapeImage load(File file) throws Invalid, IOException {
		if (!file.isFile()) {
			throw new Invalid("Not a file: " + file.getName());
		}
		if (file.length() > 8L * 1024 * 1024) {
			throw new Invalid("File too large (max 8 MB)");
		}
		if (!isPng(file)) {
			throw new Invalid(file.getName() + " is not a PNG");
		}
		BufferedImage img = ImageIO.read(file);
		if (img == null) {
			throw new Invalid(file.getName() + " could not be decoded");
		}
		return fromImage(img);
	}

	public static CapeImage fromImage(BufferedImage img) throws Invalid {
		int w = img.getWidth(), h = img.getHeight();
		int k;
		Layout layout;
		if (w % 64 == 0 && w * 32 == h * 64) {
			k = w / 64;
			layout = Layout.FULL;
		} else if (w % 22 == 0 && w * 17 == h * 22) {
			k = w / 22;
			layout = Layout.CAPE_ONLY;
		} else {
			throw new Invalid("Unsupported size " + w + "x" + h + ": use 64x32 (or 128x64, 256x128...) or 22x17 (or 44x34...)");
		}
		if (k < 1 || k > MAX_SCALE) {
			throw new Invalid("Too large: " + w + "x" + h + " (max " + 64 * MAX_SCALE + "x" + 32 * MAX_SCALE + ")");
		}
		BufferedImage out = new BufferedImage(64 * k, 32 * k, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = out.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return new CapeImage(out, k, layout);
	}

	/** Front face of the cape (u 1..11, v 1..17 in 64x32 units), nearest-neighbour upscaled. */
	public BufferedImage preview(int width) {
		int k = scale;
		BufferedImage front = texture.getSubimage(k, k, 10 * k, 16 * k);
		int height = width * 16 / 10;
		BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = out.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.drawImage(front, 0, 0, width, height, null);
		g.dispose();
		return out;
	}

	public boolean isBlank() {
		BufferedImage front = texture.getSubimage(scale, scale, 10 * scale, 16 * scale);
		for (int y = 0; y < front.getHeight(); y++) {
			for (int x = 0; x < front.getWidth(); x++) {
				if ((front.getRGB(x, y) >>> 24) != 0) {
					return false;
				}
			}
		}
		return true;
	}

	static boolean isPng(File f) throws IOException {
		InputStream in = new FileInputStream(f);
		try {
			byte[] b = new byte[8];
			new DataInputStream(in).readFully(b);
			for (int i = 0; i < 8; i++) {
				if (b[i] != PNG_MAGIC[i]) {
					return false;
				}
			}
			return true;
		} catch (java.io.EOFException e) {
			return false;
		} finally {
			in.close();
		}
	}

	/** Short content hash: preview cache key, so replacing a file never shows a stale preview. */
	public static String hash(File f) throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			InputStream in = new FileInputStream(f);
			try {
				byte[] buf = new byte[8192];
				int n;
				while ((n = in.read(buf)) > 0) {
					md.update(buf, 0, n);
				}
			} finally {
				in.close();
			}
			StringBuilder s = new StringBuilder();
			byte[] d = md.digest();
			for (int i = 0; i < 6; i++) {
				s.append(String.format("%02x", d[i]));
			}
			return s.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	/** File-system-safe display name. */
	public static String cleanName(String base) {
		String s = base.replaceAll("[^A-Za-z0-9 _.()\\-]", "_").trim();
		if (s.isEmpty()) {
			s = "cape";
		}
		return s.length() > 40 ? s.substring(0, 40) : s;
	}
}
