package confusione.launcher.ui;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

/** Sidebar icon button: vector icon, hover glow, tooltip label that slides out. */
public final class NavButton extends JComponent {

	public enum Icon { PLAY, SETTINGS, CONSOLE, CLOSE, MINIMIZE }

	private final Icon icon;
	private final Anim hover = new Anim(this, 0, 14);
	private final Anim selected = new Anim(this, 0, 12);
	private Runnable action;

	public NavButton(Icon icon, String tip) {
		this.icon = icon;
		setOpaque(false);
		setToolTipText(tip);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) { hover.set(1); }
			public void mouseExited(MouseEvent e) { hover.set(0); }
			public void mouseReleased(MouseEvent e) {
				if (contains(e.getPoint()) && action != null) {
					action.run();
				}
			}
		});
	}

	public void onClick(Runnable r) {
		action = r;
	}

	public void setSelected(boolean s) {
		selected.set(s ? 1 : 0);
	}

	@Override
	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D) g0.create();
		Theme.hq(g);
		int w = getWidth(), h = getHeight();
		float hv = hover.get(), sel = selected.get();
		boolean windowCtl = icon == Icon.CLOSE || icon == Icon.MINIMIZE;

		if (!windowCtl) {
			g.setColor(Theme.alpha(Theme.PANEL_HI, hv * 0.9f + sel));
			g.fill(new RoundRectangle2D.Float(4, 4, w - 8, h - 8, 14, 14));
		} else if (hv > 0.01f) {
			g.setColor(Theme.alpha(icon == Icon.CLOSE ? Theme.ERROR : Theme.PANEL_HI, hv * (icon == Icon.CLOSE ? 0.85f : 1f)));
			g.fill(new Ellipse2D.Float(w / 2f - 9, h / 2f - 9, 18, 18));
		}

		float cx = w / 2f, cy = h / 2f;
		float s = windowCtl ? 4f : 8f + hv * 0.6f;
		g.setStroke(new BasicStroke(windowCtl ? 1.6f : 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		if (sel > 0.02f && !windowCtl) {
			g.setPaint(Theme.accent(cx - s, cy - s, cx + s, cy + s));
		} else {
			g.setColor(Theme.mix(Theme.MUTED, Theme.TEXT, Math.max(hv, sel)));
		}

		switch (icon) {
		case PLAY: {
			GeneralPath p = new GeneralPath();
			p.moveTo(cx - s * 0.6f, cy - s * 0.85f);
			p.lineTo(cx + s * 0.85f, cy);
			p.lineTo(cx - s * 0.6f, cy + s * 0.85f);
			p.closePath();
			g.fill(p);
			break;
		}
		case SETTINGS: {
			// three sliders
			for (int i = -1; i <= 1; i++) {
				float y = cy + i * s * 0.7f;
				g.drawLine(Math.round(cx - s), Math.round(y), Math.round(cx + s), Math.round(y));
				float kx = cx + (i == 0 ? s * 0.4f : -s * 0.4f) * (1 - 2 * hv * (i == 0 ? 1 : 0.5f));
				g.fill(new Ellipse2D.Float(kx - 3f, y - 3f, 6f, 6f));
			}
			break;
		}
		case CONSOLE: {
			GeneralPath p = new GeneralPath();
			p.moveTo(cx - s, cy - s * 0.6f);
			p.lineTo(cx - s * 0.35f, cy);
			p.lineTo(cx - s, cy + s * 0.6f);
			g.draw(p);
			g.drawLine(Math.round(cx), Math.round(cy + s * 0.6f), Math.round(cx + s), Math.round(cy + s * 0.6f));
			break;
		}
		case CLOSE:
			g.setColor(Theme.mix(Theme.MUTED, Theme.TEXT, hv));
			g.drawLine(Math.round(cx - s), Math.round(cy - s), Math.round(cx + s), Math.round(cy + s));
			g.drawLine(Math.round(cx + s), Math.round(cy - s), Math.round(cx - s), Math.round(cy + s));
			break;
		case MINIMIZE:
			g.setColor(Theme.mix(Theme.MUTED, Theme.TEXT, hv));
			g.drawLine(Math.round(cx - s), Math.round(cy), Math.round(cx + s), Math.round(cy));
			break;
		}
		g.dispose();
	}
}
