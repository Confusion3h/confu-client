package confusione.launcher.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

/**
 * Animated confusione mark: the C draws itself in on start, the dot breathes on the
 * ambient clock, and while busy the ring opens into a spinning arc.
 */
public final class LogoMark extends JComponent {

	private final Anim spin = new Anim(this, 0, 6);
	private final Anim intro = new Anim(this, 0, 2.6f);

	public LogoMark() {
		setOpaque(false);
		Ambient.register(this);
		intro.set(1);
	}

	public void setBusy(boolean busy) {
		spin.set(busy ? 1 : 0);
	}

	@Override
	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D) g0.create();
		Theme.hq(g);
		float s = Math.min(getWidth(), getHeight());
		float t = Ambient.time();
		float busy = spin.get();
		float sweep = 360 - 270 * busy;
		float rot = 90 - t * 300f * busy;
		Mark.paint(g, (getWidth() - s) / 2, (getHeight() - s) / 2, s, sweep, rot, intro.get(), (float) Math.sin(t * 2.2f));
		g.dispose();
	}
}
