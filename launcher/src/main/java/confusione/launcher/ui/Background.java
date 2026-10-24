package confusione.launcher.ui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Aurora backdrop: three soft color blobs drifting on Lissajous paths. They are
 * painted into a 1/8-scale buffer and bilinearly upscaled, which is what makes them
 * soft and keeps the per-frame cost tiny (~120x75 pixels of gradient fill plus
 * one bilinear blit).
 */
final class Background {

	private static final int DOWNSCALE = 8;
	private BufferedImage small;
	private float lastTime = Float.NaN;

	void paint(Graphics2D g, int w, int h, float t, float energy) {
		int sw = Math.max(1, w / DOWNSCALE), sh = Math.max(1, h / DOWNSCALE);
		if (small == null || small.getWidth() != sw || small.getHeight() != sh) {
			small = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_RGB);
			lastTime = Float.NaN;
		}
		if (t != lastTime) {
			lastTime = t;
			render(small.createGraphics(), sw, sh, t, energy);
		}
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(small, 0, 0, w, h, null);

	}

	private void render(Graphics2D g, int w, int h, float t, float energy) {
		g.setColor(Theme.BG);
		g.fillRect(0, 0, w, h);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		float s = 0.12f + 0.25f * energy; // energy rises while launching
		blob(g, w * (0.25f + 0.18f * (float) Math.sin(t * s * 1.0f)), h * (0.35f + 0.22f * (float) Math.cos(t * s * 1.3f)), w * 0.55f, Theme.A1, 0.16f); // grey-on-black glows: white is far brighter than the old hues
		blob(g, w * (0.75f + 0.15f * (float) Math.cos(t * s * 0.8f + 1)), h * (0.65f + 0.20f * (float) Math.sin(t * s * 1.1f + 2)), w * 0.50f, Theme.A2, 0.11f + 0.06f * energy);
		blob(g, w * (0.55f + 0.25f * (float) Math.sin(t * s * 0.6f + 4)), h * (0.95f + 0.10f * (float) Math.cos(t * s * 0.9f)), w * 0.40f, Theme.A3, 0.10f + 0.06f * energy);
		// vignette baked into the small buffer too: no full-resolution alpha pass per frame
		g.setPaint(new RadialGradientPaint(new Point2D.Float(w / 2f, h / 2f), Math.max(w, h) * 0.75f,
				new float[] { 0.45f, 1f }, new Color[] { new Color(0, 0, 0, 0), new Color(0, 0, 0, 160) }));
		g.fillRect(0, 0, w, h);
		g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 0), 0, h, new Color(0, 0, 0, 80)));
		g.fillRect(0, 0, w, h);
		g.dispose();
	}

	private static void blob(Graphics2D g, float x, float y, float r, Color c, float a) {
		g.setPaint(new RadialGradientPaint(new Point2D.Float(x, y), r, new float[] { 0f, 1f },
				new Color[] { Theme.alpha(c, a), Theme.alpha(c, 0f) }, MultipleGradientPaint.CycleMethod.NO_CYCLE));
		g.fillRect(0, 0, (int) (x + r) + 1, (int) (y + r) + 1);
	}
}
