package confusione.launcher.ui;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;

/**
 * Placeholder "C" mark (real logo arrives in phase 3): a gradient arc whose opening
 * slowly breathes; while busy the arc spins and a second comet arc orbits it.
 */
public final class LogoMark extends JComponent {

	private final Anim spin = new Anim(this, 0, 6);

	public LogoMark() {
		setOpaque(false);
		Ambient.register(this);
	}

	public void setBusy(boolean busy) {
		spin.set(busy ? 1 : 0);
	}

	@Override
	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D) g0.create();
		Theme.hq(g);
		float s = Math.min(getWidth(), getHeight());
		float pad = s * 0.14f;
		float d = s - pad * 2;
		float t = Ambient.time();
		float busy = spin.get();

		float stroke = s * 0.13f;
		float open = 70 + 12 * (float) Math.sin(t * 1.4f) * (1 - busy);
		float rot = busy * (t * 260f % 360f);
		float start = open / 2 + rot;
		g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setPaint(Theme.accent(pad, pad, pad + d, pad + d));
		g.draw(new Arc2D.Float(pad, pad, d, d, start, 360 - open, Arc2D.OPEN));

		if (busy > 0.01f) {
			float r = d * 0.5f + stroke * 0.9f;
			float cx = s / 2, cy = s / 2;
			g.setStroke(new BasicStroke(stroke * 0.35f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.setColor(Theme.alpha(Theme.A3, busy));
			g.draw(new Arc2D.Float(cx - r, cy - r, r * 2, r * 2, -t * 420f % 360f, 50, Arc2D.OPEN));
		}

		// core dot sits in the C's opening
		float dot = stroke * 0.8f;
		double a = Math.toRadians(-rot);
		float dx = (float) (s / 2 + Math.cos(a) * d * 0.5f), dy = (float) (s / 2 + Math.sin(a) * d * 0.5f);
		g.setColor(Theme.alpha(Theme.A3, 0.9f + 0.1f * (float) Math.sin(t * 3)));
		g.fill(new Ellipse2D.Float(dx - dot / 2, dy - dot / 2, dot, dot));
		g.dispose();
	}
}
