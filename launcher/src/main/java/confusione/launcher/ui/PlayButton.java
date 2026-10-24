package confusione.launcher.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

/**
 * The big call to action. Idle: gradient pill with a hover lift + light sweep.
 * Busy: the pill becomes a progress track whose fill eases to the real value.
 * Running: calm outline with a pulsing live dot.
 */
public final class PlayButton extends JComponent {

	public enum State { IDLE, BUSY, RUNNING }

	private State state = State.IDLE;
	private String label = "PLAY";
	private String sub = "";
	private final Anim hover = new Anim(this, 0, 12);
	private final Anim press = new Anim(this, 0, 22);
	private final Anim busyMix = new Anim(this, 0, 8);
	private final Anim fill = new Anim(this, 0, 5);
	private final Anim sweep = new Anim(this, -0.3f, 2.2f);
	private boolean indeterminate;
	private Runnable action;

	public PlayButton() {
		setOpaque(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		Ambient.register(this);
		addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				hover.set(1);
				if (state == State.IDLE) {
					sweep.snap(-0.3f);
					sweep.set(1.3f); // one light pass per hover, not a loop
				}
			}
			public void mouseExited(MouseEvent e) { hover.set(0); press.set(0); }
			public void mousePressed(MouseEvent e) { press.set(1); }
			public void mouseReleased(MouseEvent e) {
				press.set(0);
				if (contains(e.getPoint()) && state == State.IDLE && action != null) {
					action.run();
				}
			}
		});
	}

	public void onClick(Runnable r) {
		action = r;
	}

	public void setState(State s, String label) {
		this.state = s;
		this.label = label;
		busyMix.set(s == State.BUSY ? 1 : 0);
		if (s != State.BUSY) {
			fill.snap(0);
		}
		setCursor(Cursor.getPredefinedCursor(s == State.IDLE ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
		repaint();
	}

	public void setSub(String sub) {
		this.sub = sub == null ? "" : sub;
		repaint();
	}

	public void setProgress(double p) {
		indeterminate = p < 0;
		if (!indeterminate) {
			fill.set((float) Math.max(fill.target(), p));
		}
	}

	@Override
	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D) g0.create();
		Theme.hq(g);
		float w = getWidth(), h = getHeight();
		float hv = state == State.IDLE ? hover.get() : 0;
		float pr = press.get();
		float bm = busyMix.get();
		float t = Ambient.time();

		float inset = 6 - hv * 2 + pr * 3;
		float x = inset, y = inset + 2 - hv * 2 + pr * 1.5f, bw = w - inset * 2, bh = h - inset * 2 - 4;
		float arc = bh;
		Shape pill = new RoundRectangle2D.Float(x, y, bw, bh, arc, arc);

		// glow
		if (state != State.RUNNING) {
			for (int i = 3; i >= 1; i--) {
				float gpad = i * 3.5f;
				g.setColor(Theme.alpha(Theme.A2, (0.05f + 0.06f * hv) * (1 - bm * 0.5f)));
				g.fill(new RoundRectangle2D.Float(x - gpad, y - gpad + 3, bw + gpad * 2, bh + gpad * 2, arc + gpad * 2, arc + gpad * 2));
			}
		}

		if (state == State.RUNNING) {
			g.setColor(Theme.PANEL);
			g.fill(pill);
			g.setColor(Theme.STROKE);
			g.draw(pill);
		} else {
			// track (visible while busy)
			g.setColor(Theme.alpha(Theme.PANEL_HI, bm));
			g.fill(pill);
			// gradient: full pill when idle, progress-width while busy
			float fw = bw * (1 - bm) + bw * bm * (indeterminate ? 1 : fill.get());
			if (fw > 1) {
				Graphics2D gc = (Graphics2D) g.create();
				gc.clip(pill);
				gc.setPaint(Theme.accent(x, y, x + bw, y + bh));
				if (indeterminate && bm > 0.5f) {
					gc.setComposite(AlphaComposite.SrcOver.derive(0.35f + 0.25f * (float) Math.sin(t * 4)));
				}
				gc.fill(new RoundRectangle2D.Float(x, y, fw, bh, arc, arc));
				// moving stripes while busy
				if (bm > 0.01f) {
					gc.setComposite(AlphaComposite.SrcOver.derive(0.12f * bm));
					gc.setColor(Color.WHITE);
					float off = (t * 40) % 24;
					for (float sx = x - bh + off - 24; sx < x + fw; sx += 24) {
						int[] xs = { (int) sx, (int) (sx + 12), (int) (sx + 12 + bh), (int) (sx + bh) };
						int[] ys = { (int) (y + bh), (int) (y + bh), (int) y, (int) y };
						gc.fillPolygon(xs, ys, 4);
					}
				}
				gc.dispose();
			}
			// hover light sweep
			float sw = sweep.get();
			if (bm < 0.5f && sw > -0.25f && sw < 1.25f) {
				Graphics2D gs = (Graphics2D) g.create();
				gs.clip(pill);
				float sx = x + bw * sw;
				gs.setPaint(new GradientPaint(sx - 60, 0, new Color(255, 255, 255, 0), sx, 0, new Color(255, 255, 255, 70), true));
				gs.fill(new RoundRectangle2D.Float(sx - 60, y, 120, bh, 0, 0));
				gs.dispose();
			}
			// top highlight
			g.setPaint(new GradientPaint(0, y, new Color(255, 255, 255, 55), 0, y + bh * 0.6f, new Color(255, 255, 255, 0)));
			g.fill(new RoundRectangle2D.Float(x + 1, y + 1, bw - 2, bh * 0.6f, arc, arc));
		}

		// label
		g.setFont(Theme.semibold(bh * 0.30f));
		FontMetrics fm = g.getFontMetrics();
		String text = label;
		float ty = y + bh / 2 + fm.getAscent() * 0.36f - (sub.isEmpty() ? 0 : bh * 0.1f);
		float tx = x + (bw - fm.stringWidth(text)) / 2;
		if (state == State.RUNNING) {
			float pulse = 0.5f + 0.5f * (float) Math.sin(t * 3);
			float dx = tx - 16;
			g.setColor(Theme.alpha(Theme.OK, 0.25f * pulse));
			g.fill(new Ellipse2D.Float(dx - 6, ty - fm.getAscent() * 0.36f - 6, 12, 12));
			g.setColor(Theme.OK);
			g.fill(new Ellipse2D.Float(dx - 3.5f, ty - fm.getAscent() * 0.36f - 3.5f, 7, 7));
		}
		g.setColor(Theme.TEXT);
		g.drawString(text, tx, ty);
		if (!sub.isEmpty()) {
			g.setFont(Theme.regular(bh * 0.17f));
			FontMetrics fs = g.getFontMetrics();
			g.setColor(Theme.alpha(Theme.TEXT, 0.75f));
			g.drawString(sub, x + (bw - fs.stringWidth(sub)) / 2, ty + bh * 0.24f);
		}
		g.dispose();
	}
}
