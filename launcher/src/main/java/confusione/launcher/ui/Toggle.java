package confusione.launcher.ui;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

/** Labeled switch row; the knob slides and the track cross-fades to the accent. */
public final class Toggle extends JComponent {

	public interface Listener { void changed(boolean on); }

	private final String label, hint;
	private boolean on;
	private final Anim knob;
	private final Anim hover = new Anim(this, 0, 14);
	private Listener listener;

	public Toggle(String label, String hint, boolean on) {
		this.label = label;
		this.hint = hint;
		this.on = on;
		this.knob = new Anim(this, on ? 1 : 0, 14);
		setOpaque(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) { hover.set(1); }
			public void mouseExited(MouseEvent e) { hover.set(0); }
			public void mouseReleased(MouseEvent e) {
				if (contains(e.getPoint())) {
					Toggle.this.on = !Toggle.this.on;
					knob.set(Toggle.this.on ? 1 : 0);
					if (listener != null) {
						listener.changed(Toggle.this.on);
					}
				}
			}
		});
	}

	public void setListener(Listener l) {
		listener = l;
	}

	@Override
	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D) g0.create();
		Theme.hq(g);
		float w = getWidth(), h = getHeight(), k = knob.get();
		g.setColor(Theme.mix(Theme.PANEL, Theme.PANEL_HI, hover.get()));
		g.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
		g.setFont(Theme.semibold(13));
		g.setColor(Theme.TEXT);
		g.drawString(label, 14, h / 2 - 2);
		g.setFont(Theme.regular(11));
		g.setColor(Theme.MUTED);
		g.drawString(hint, 14, h / 2 + 13);

		float tw = 40, th = 22, tx = w - tw - 14, ty = (h - th) / 2;
		RoundRectangle2D.Float track = new RoundRectangle2D.Float(tx, ty, tw, th, th, th);
		g.setColor(Theme.PANEL_HI);
		g.fill(track);
		if (k > 0.01f) {
			float base = Theme.compositeAlpha(g);
			g.setComposite(java.awt.AlphaComposite.SrcOver.derive(base * k));
			g.setPaint(Theme.accent2(tx, 0, tx + tw, 0));
			g.fill(track);
			g.setComposite(java.awt.AlphaComposite.SrcOver.derive(base));
		}
		float d = th - 6;
		g.setColor(Theme.mix(Theme.TEXT, Theme.ON_ACCENT, k));
		g.fill(new Ellipse2D.Float(tx + 3 + (tw - d - 6) * k, ty + 3, d, d));
		g.dispose();
	}
}
