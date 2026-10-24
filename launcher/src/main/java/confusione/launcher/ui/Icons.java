package confusione.launcher.ui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/** App icons rendered from branding/confusione-app-icon*.svg, plus vector mark images. */
final class Icons {

	private static final int[] SIZES = { 16, 32, 64, 128, 256 };
	private static final Map<Integer, BufferedImage> marks = new HashMap<Integer, BufferedImage>();

	private Icons() {}

	static List<Image> windowIcons() {
		List<Image> list = new ArrayList<Image>();
		for (int s : SIZES) {
			InputStream in = Icons.class.getResourceAsStream("/confusione/icons/icon_" + s + "x" + s + ".png");
			if (in == null) {
				continue;
			}
			try {
				try {
					list.add(ImageIO.read(in));
				} finally {
					in.close();
				}
			} catch (java.io.IOException ignored) {
			}
		}
		return list;
	}

	/** Static gradient mark (no background), cached per size. */
	static BufferedImage mark(int s) {
		BufferedImage img = marks.get(s);
		if (img == null) {
			img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics2D g = img.createGraphics();
			Theme.hq(g);
			Mark.paint(g, 0, 0, s, 360, 0, 1, 0);
			g.dispose();
			marks.put(s, img);
		}
		return img;
	}
}
