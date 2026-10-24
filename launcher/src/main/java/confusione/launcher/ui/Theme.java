package confusione.launcher.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;

/** Colors, fonts and paint helpers for the custom-drawn UI. */
public final class Theme {

	// Monochrome only: black, greys and white (see docs/branding.md, launcher palette).
	public static final Color BG = new Color(0x0A0A0A);
	public static final Color PANEL = new Color(255, 255, 255, 14);
	public static final Color PANEL_HI = new Color(255, 255, 255, 26);
	public static final Color STROKE = new Color(255, 255, 255, 30);
	public static final Color TEXT = new Color(0xF5F5F5);
	public static final Color MUTED = new Color(0x9A9A9A);
	public static final Color ERROR = new Color(0xD6D6D6); // errors read by wording, not hue
	public static final Color OK = new Color(0xFFFFFF);

	public static final Color A1 = new Color(0xFFFFFF); // white
	public static final Color A2 = new Color(0xC8C8C8); // light grey
	public static final Color A3 = new Color(0x8C8C8C); // mid grey
	/** Quiet steel blue for Play, Sign in and the active nav. The rest of the UI stays grey. */
	public static final Color ACCENT = new Color(0x7C93A8);
	public static final Color ACCENT_HI = new Color(0xD5E2EC);
	/** Text/knobs drawn on top of the (light) accent gradient. */
	public static final Color ON_ACCENT = new Color(0x111111);

	private static final Font REGULAR = load("Inter-Regular.ttf", Font.PLAIN);
	private static final Font SEMIBOLD = load("Inter-SemiBold.ttf", Font.BOLD);

	private Theme() {}

	public static Font regular(float size) {
		return REGULAR.deriveFont(size);
	}

	public static Font semibold(float size) {
		return SEMIBOLD.deriveFont(size);
	}

	private static Font load(String name, int fallbackStyle) {
		InputStream in = Theme.class.getResourceAsStream("/confusione/fonts/" + name);
		if (in != null) {
			try {
				try {
					return Font.createFont(Font.TRUETYPE_FONT, in);
				} finally {
					in.close();
				}
			} catch (FontFormatException e) {
				// fall through
			} catch (IOException e) {
				// fall through
			}
		}
		return new Font(Font.SANS_SERIF, fallbackStyle, 12);
	}

	public static Paint accent(float x0, float y0, float x1, float y1) {
		if (x0 == x1 && y0 == y1) {
			return A1;
		}
		return new LinearGradientPaint(x0, y0, x1, y1, new float[] { 0f, 0.55f, 1f }, new Color[] { A1, A2, A3 });
	}

	public static Paint accent2(float x0, float y0, float x1, float y1) {
		return new GradientPaint(x0, y0, A1, x1, y1, A2);
	}

	public static void hq(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
	}

	/** Alpha already applied to this graphics by a parent fade. */
	public static float compositeAlpha(Graphics2D g) {
		Composite c = g.getComposite();
		if (c instanceof AlphaComposite) {
			return ((AlphaComposite) c).getAlpha();
		}
		return 1f;
	}

	/** SrcOver at parentAlpha * factor, so a child fade cannot punch through a page transition. */
	public static void modulate(Graphics2D g, float factor) {
		float a = compositeAlpha(g) * factor;
		if (a < 0f) {
			a = 0f;
		} else if (a > 1f) {
			a = 1f;
		}
		g.setComposite(AlphaComposite.SrcOver.derive(a));
	}

	public static Color alpha(Color c, float a) {
		int al = Math.max(0, Math.min(255, Math.round(c.getAlpha() * a)));
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), al);
	}

	public static Color mix(Color a, Color b, float t) {
		t = Math.max(0, Math.min(1, t));
		return new Color(
				Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
				Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
				Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
				Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
	}
}
