package confusione.launcher.ui;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

/** Stepped slider (RAM) with an easing knob and value bubble. */
public final class Slider extends JComponent {

	public interface Listener { void changed(int value); }

	private final String label;
	private final int min, max, step;
	private int value;
	private final Anim pos;
	private final Anim hover = new Anim(this, 0, 14);
	private Listener listener;

	public Slider(String label, int min, int max, int step, int value) {
		this.label = label;
		this.min = min;
		this.max = max;
		this.step = step;
		this.value = clamp(value);
		this.pos = new Anim(this, frac(this.value), 16);
		setOpaque(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		MouseAdapter m = new MouseAdapter() {
			public void mousePressed(MouseEvent e) { drag(e.getX()); }
			public void mouseDragged(MouseEvent e) { drag(e.getX()); }
			public void mouseEntered(MouseEvent e) { hover.set(1); }
			public void mouseExited(MouseEvent e) { hover.set(0); }
		};
		addMouseListener(m);
		addMouseMotionListener(m);
	}

	public void setListener(Listener l) {
		listener = l;
	}

	public int getValue() {
		return value;
	}

	private int clamp(int v) {
		v = Math.round((float) v / step) * step;
		return Math.max(min, Math.min(max, v));
	}

	private float frac(int v) {
		return (v - min) / (float) (max - min);
	}

	private void drag(int mx) {
		float f = (mx - 14) / (float) (getWidth() - 28);
		int v = clamp(Math.round(min + f * (max - min)));
		if (v != value) {
			value = v;
			pos.set(frac(v));
			if (listener != null) {
				listener.changed(v);
			}
			repaint();
		}
	}

	@Override
	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D) g0.create();
		Theme.hq(g);
		float w = getWidth(), h = getHeight();
		g.setColor(Theme.PANEL);
		g.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));
		g.setFont(Theme.semibold(10));
		g.setColor(Theme.MUTED);
		g.drawString(label.toUpperCase(), 14, 15);
		String v = value >= 1024 && value % 1024 == 0 ? (value / 1024) + " GB" : value + " MB";
		g.setFont(Theme.semibold(12));
		g.setColor(Theme.TEXT);
		g.drawString(v, w - 14 - g.getFontMetrics().stringWidth(v), 16);

		float ty = h - 16, tx = 14, tw = w - 28;
		g.setColor(Theme.PANEL_HI);
		g.fill(new RoundRectangle2D.Float(tx, ty - 2, tw, 4, 4, 4));
		float kx = tx + tw * pos.get();
		g.setPaint(Theme.accent(tx, 0, tx + tw, 0));
		g.fill(new RoundRectangle2D.Float(tx, ty - 2, kx - tx, 4, 4, 4));
		float kr = 6 + hover.get() * 2;
		g.setColor(Theme.TEXT);
		g.fill(new Ellipse2D.Float(kx - kr, ty - kr, kr * 2, kr * 2));
		g.dispose();
	}
}
