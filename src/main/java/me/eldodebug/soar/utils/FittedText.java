package me.eldodebug.soar.utils;

import java.awt.Color;

import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Font;

/** Centers a welcome line, wrapping to two lines or shrinking so it stays inside the margins. */
public final class FittedText {

	private FittedText() {}

	public static void drawCentered(NanoVGManager nvg, String text, float cx, float cy, float maxWidth, float size, Font font, Color color) {
		if (text == null || text.isEmpty()) {
			return;
		}
		float width = Math.max(80, maxWidth);
		float s = size;
		while (s > 15f && nvg.getTextWidth(text, s, font) > width * 1.85f) {
			s -= 1f;
		}
		if (nvg.getTextWidth(text, s, font) <= width) {
			nvg.drawCenteredText(text, cx, cy, color, s, font);
			return;
		}
		int split = splitAt(nvg, text, width, s, font);
		String a = text.substring(0, split).trim();
		String b = text.substring(split).trim();
		float h = Math.max(s, nvg.getTextHeight("Ag", s, font));
		float gap = 4f;
		nvg.drawCenteredText(a, cx, cy - (h + gap) * 0.5f, color, s, font);
		if (!b.isEmpty()) {
			nvg.drawCenteredText(b, cx, cy + (h + gap) * 0.5f, color, s, font);
		}
	}

	private static int splitAt(NanoVGManager nvg, String text, float width, float size, Font font) {
		int best = text.length() / 2;
		float bestScore = Float.MAX_VALUE;
		for (int i = 1; i < text.length(); i++) {
			if (text.charAt(i) != ' ') {
				continue;
			}
			float wa = nvg.getTextWidth(text.substring(0, i).trim(), size, font);
			float wb = nvg.getTextWidth(text.substring(i).trim(), size, font);
			float score = Math.max(wa, wb) + Math.abs(wa - wb) * 0.15f;
			if (score < bestScore) {
				bestScore = score;
				best = i;
			}
		}
		return best;
	}
}
