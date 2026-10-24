package me.eldodebug.soar.utils;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.Display;

import me.eldodebug.soar.logger.GlideLogger;

/** Sets the LWJGL2 window icon to the CONFUSION3H mark (16/32/64 px). */
public final class WindowIcon {

	private static final int[] SIZES = { 16, 32, 64 };

	private WindowIcon() {}

	public static boolean apply() {
		try {
			ByteBuffer[] icons = new ByteBuffer[SIZES.length];
			for (int i = 0; i < SIZES.length; i++) {
				String path = "/assets/minecraft/soar/logo/icon_" + SIZES[i] + "x" + SIZES[i] + ".png";
				InputStream in = WindowIcon.class.getResourceAsStream(path);
				if (in == null) {
					return false;
				}
				BufferedImage img;
				try {
					img = ImageIO.read(in);
				} finally {
					in.close();
				}
				icons[i] = rgba(img);
			}
			Display.setIcon(icons);
			return true;
		} catch (Exception e) {
			GlideLogger.error("Failed to set window icon", e);
			return false;
		}
	}

	private static ByteBuffer rgba(BufferedImage img) {
		int w = img.getWidth(), h = img.getHeight();
		int[] px = img.getRGB(0, 0, w, h, null, 0, w);
		ByteBuffer buf = ByteBuffer.allocate(w * h * 4);
		for (int p : px) {
			buf.put((byte) (p >> 16)).put((byte) (p >> 8)).put((byte) p).put((byte) (p >> 24));
		}
		buf.flip();
		return buf;
	}
}
