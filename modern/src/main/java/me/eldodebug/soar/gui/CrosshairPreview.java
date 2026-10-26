package me.eldodebug.soar.gui;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import me.eldodebug.confu.module.impl.CrosshairModule;
import me.eldodebug.soar.management.nanovg.NanoVGManager;

/** The 1.8.9 crosshair settings preview (sky, grass, pixel mask of the chosen style) for the 1.20.1 module. */
public final class CrosshairPreview {

	private static boolean[][][] masks;

	private CrosshairPreview() {
	}

	public static void draw(NanoVGManager nvg, CrosshairModule mod, float x, float y, float size) {
		nvg.drawRoundedRect(x, y, size, size, 8, new Color(112, 150, 205));
		nvg.drawRoundedRectVarying(x, y + size * 0.6F, size, size * 0.4F, 0, 0, 8, 8, new Color(92, 132, 58));
		boolean custom = mod.customEnabled();
		boolean[][] mask = custom ? mask(mod.styleIndex()) : vanillaMask();
		if (mask == null) {
			return;
		}
		int rgb = custom ? mod.rgb() : 0xFFFFFF;
		Color c = new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, (int) (255 * (custom ? mod.opacityValue() : 1F)));
		float px = size / 32F * (custom ? mod.scaleValue() : 1F);
		float ox = x + size / 2 - 8 * px, oy = y + size / 2 - 8 * px;
		for (int j = 0; j < 16; j++) {
			for (int i = 0; i < 16; i++) {
				if (mask[j][i]) {
					nvg.drawRect(ox + i * px, oy + j * px, px + 0.05F, px + 0.05F, c);
				}
			}
		}
	}

	private static boolean[][] vanillaMask() {
		boolean[][] m = new boolean[16][16];
		for (int k = 3; k <= 11; k++) {
			m[7][k] = true;
			m[k][7] = true;
		}
		return m;
	}

	private static boolean[][] mask(int index) {
		if (masks == null) {
			masks = new boolean[9][][];
			try (InputStream in = CrosshairPreview.class.getResourceAsStream("/assets/confu/textures/gui/crosshair.png")) {
				BufferedImage img = ImageIO.read(in);
				int cell = Math.max(1, img.getWidth() / 256);
				for (int t = 0; t < 9; t++) {
					boolean[][] m = new boolean[16][16];
					for (int j = 0; j < 16; j++) {
						for (int i = 0; i < 16; i++) {
							m[j][i] = (img.getRGB((t * 16 + i) * cell, j * cell) >>> 24) > 40;
						}
					}
					masks[t] = m;
				}
			} catch (Exception e) {
				return null;
			}
		}
		return index >= 0 && index < masks.length ? masks[index] : null;
	}
}
