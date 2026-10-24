package confusione.launcher.ui;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

/**
 * Holds the pages; switching cross-fades and slides (old page drifts out, new one in).
 * The outgoing page is hidden immediately (so it gets no input) and painted manually
 * until the transition finishes.
 */
public final class PageHost extends JComponent {

	private JComponent current, previous;
	private int direction = 1;
	private final Anim t = new Anim(this, 1, 11);

	public PageHost() {
		setOpaque(false);
		setLayout(null);
	}

	public void addPage(JComponent page) {
		page.setVisible(false);
		add(page);
		if (current == null) {
			current = page;
			page.setVisible(true);
		}
	}

	public JComponent current() {
		return current;
	}

	public void show(JComponent page, int dir) {
		if (page == current) {
			return;
		}
		previous = current;
		current = page;
		direction = dir;
		previous.setVisible(false);
		current.setVisible(true);
		t.snap(0);
		t.set(1);
		if (page instanceof Entrance) {
			((Entrance) page).enter();
		}
	}

	/** Pages that play their own staggered entrance. */
	public interface Entrance {
		void enter();
	}

	@Override
	public void doLayout() {
		for (Component c : getComponents()) {
			c.setBounds(0, 0, getWidth(), getHeight());
		}
	}

	@Override
	protected void paintChildren(Graphics g0) {
		float p = t.get();
		boolean fading = previous != null && p < 0.999f;
		if (!fading) {
			if (current != null) {
				current.paint(g0);
			}
			return;
		}
		// Paint each page into its own image, then blit with the transition alpha.
		// Home's news cards (and any child that calls setComposite) would otherwise
		// replace this alpha and stay fully opaque for the rest of the ~0.6s ease.
		float outA = Math.max(0f, 1f - p * 1.6f);
		if (outA > 0.02f) {
			blit(g0, previous, outA, -direction * 18f * p);
		}
		if (current != null) {
			blit(g0, current, Math.min(1f, p), direction * 24f * (1f - p));
		}
	}

	private void blit(Graphics g0, JComponent page, float alpha, float dy) {
		int w = Math.max(1, getWidth());
		int h = Math.max(1, getHeight());
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D pg = img.createGraphics();
		pg.setComposite(AlphaComposite.SrcOver);
		page.paint(pg);
		pg.dispose();
		Graphics2D g = (Graphics2D) g0.create();
		g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0f, Math.min(1f, alpha))));
		g.translate(0, dy);
		g.drawImage(img, 0, 0, null);
		g.dispose();
	}
}
