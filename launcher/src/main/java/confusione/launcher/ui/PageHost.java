package confusione.launcher.ui;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

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
		if (previous != null && p < 0.999f) {
			Graphics2D g = (Graphics2D) g0.create();
			g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0, 1 - p * 1.6f)));
			g.translate(0, -direction * 18 * p);
			previous.paint(g);
			g.dispose();
		}
		if (current != null) {
			Graphics2D g = (Graphics2D) g0.create();
			g.setComposite(AlphaComposite.SrcOver.derive(Math.min(1, p)));
			g.translate(0, direction * 24 * (1 - p));
			current.paint(g);
			g.dispose();
		}
	}
}
