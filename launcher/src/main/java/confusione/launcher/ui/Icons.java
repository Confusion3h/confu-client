package confusione.launcher.ui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/** Static placeholder "C" mark rendered once per size (window icon, sidebar). */
final class Icons {

	private static final Map<Integer, BufferedImage> cache = new HashMap<Integer, BufferedImage>();

	private Icons() {}

	static BufferedImage app(int s) {
		BufferedImage img = cache.get(s);
		if (img != null) {
			return img;
		}
		img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		Theme.hq(g);
		float pad = s * 0.16f, d = s - pad * 2, stroke = s * 0.15f;
		g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setPaint(Theme.accent(pad, pad, pad + d, pad + d));
		g.draw(new Arc2D.Float(pad, pad, d, d, 35, 290, Arc2D.OPEN));
		g.setColor(Theme.A3);
		float dot = stroke * 0.8f;
		g.fill(new Ellipse2D.Float(pad + d - dot / 2, s / 2f - dot / 2, dot, dot));
		g.dispose();
		cache.put(s, img);
		return img;
	}
}
