package me.eldodebug.confu.render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public final class Draw {

	private Draw() {
	}

	/** Rounded rectangle drawn as fills: a few rows per corner plus one body fill. Needs no shader. */
	public static void roundRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
		if (width <= 0 || height <= 0) {
			return;
		}
		int r = Math.min(radius, Math.min(width, height) / 2);
		if (r <= 0) {
			context.fill(x, y, x + width, y + height, color);
			return;
		}
		for (int row = 0; row < r; row++) {
			int inset = cornerInset(r, row);
			context.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
			context.fill(x + inset, y + height - 1 - row, x + width - inset, y + height - row, color);
		}
		context.fill(x, y + r, x + width, y + height - r, color);
	}

	private static int cornerInset(int radius, int row) {
		double dy = radius - row - 0.5D;
		double half = Math.sqrt(Math.max(0D, (double) radius * radius - dy * dy));
		return (int) Math.round(radius - half);
	}

	public static void text(DrawContext context, TextRenderer renderer, String text, int x, int y, int color) {
		context.drawText(renderer, text, x, y, color, false);
	}

	public static void textCentered(DrawContext context, TextRenderer renderer, String text, int centerX, int y, int color) {
		context.drawText(renderer, text, centerX - renderer.getWidth(text) / 2, y, color, false);
	}

	public static List<String> wrap(TextRenderer renderer, String text, int maxWidth) {
		List<String> lines = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		for (String word : text.split(" ")) {
			String candidate = line.isEmpty() ? word : line + " " + word;
			if (renderer.getWidth(candidate) <= maxWidth || line.isEmpty()) {
				line.setLength(0);
				line.append(candidate);
			} else {
				lines.add(line.toString());
				line.setLength(0);
				line.append(word);
			}
		}
		if (!line.isEmpty()) {
			lines.add(line.toString());
		}
		return lines;
	}
}
