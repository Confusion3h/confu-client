package confusione.launcher.ui;

import java.awt.Image;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/** App icons rendered from branding/CONFUSION3H-app-icon-mono.svg (the Windows icon artwork). */
final class Icons {

	private static final int[] SIZES = { 16, 32, 64, 128, 256 };

	private Icons() {}

	static List<Image> windowIcons() {
		List<Image> list = new ArrayList<Image>();
		for (int s : SIZES) {
			Image img = load("/confusione/icons/icon_" + s + "x" + s + ".png");
			if (img != null) {
				list.add(img);
			}
		}
		return list;
	}

	/** 512 px artwork for the macOS Dock tile. */
	static Image dockImage() {
		return load("/confusione/icons/icon_512x512.png");
	}

	private static Image load(String resource) {
		InputStream in = Icons.class.getResourceAsStream(resource);
		if (in == null) {
			return null;
		}
		try {
			return ImageIO.read(in);
		} catch (java.io.IOException ignored) {
			return null;
		} finally {
			try {
				in.close();
			} catch (java.io.IOException ignored) {
			}
		}
	}
}
