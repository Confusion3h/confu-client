package confusione.launcher.ui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * The CONFUSION3H mark in Java2D, same geometry as branding/CONFUSION3H-mark.svg
 * (256-unit box): ring r110 w16, C arc r62 w20 from +40deg sweeping 280deg, tail arc
 * r30 curling inward, centre dot r11. Animated parameters only rotate/trim parts.
 */
final class Mark {

	private Mark() {}

	/**
	 * @param ringSweep 360 = full ring; less draws a rotating arc (busy state)
	 * @param ringRot   degrees
	 * @param cTrim     0..1 portion of the C drawn (intro)
	 */
	static void paint(Graphics2D g0, float x, float y, float size, float ringSweep, float ringRot, float cTrim, float dotPulse) {
		Graphics2D g = (Graphics2D) g0.create();
		g.translate(x, y);
		g.scale(size / 256f, size / 256f);
		g.setPaint(Theme.accent(28, 28, 228, 228));

		g.setStroke(new BasicStroke(16, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		if (ringSweep >= 359.9f) {
			g.draw(new Ellipse2D.Float(18, 18, 220, 220));
		} else {
			g.draw(new Arc2D.Float(18, 18, 220, 220, ringRot, ringSweep, Arc2D.OPEN));
		}

		g.setStroke(new BasicStroke(20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(cPath(cTrim));

		float d = 22 * (1 + 0.12f * dotPulse);
		g.setColor(Theme.A3);
		g.fill(new Ellipse2D.Float(128 - d / 2, 128 - d / 2, d, d));
		g.dispose();
	}

	private static Path2D cPath(float trim) {
		// Java2D arcs: angles counter-clockwise from 3 o'clock, y up. SVG +40deg (y-down) = -40 here.
		Path2D p = new Path2D.Float();
		float main = 280 * Math.min(1, trim / 0.85f);
		p.append(new Arc2D.Float(128 - 62, 128 - 62, 124, 124, -40, -main, Arc2D.OPEN), false);
		if (trim > 0.85f) {
			// tail = the SVG "A 30 30 0 0 0" segment: circle centre (164.84, 116.19), 69.2deg -> 140.1deg
			float k = (trim - 0.85f) / 0.15f;
			p.append(new Arc2D.Float(164.84f - 30, 116.19f - 30, 60, 60, 69.2f, 70.9f * k, Arc2D.OPEN), true);
		}
		return p;
	}
}
