package confusione.launcher.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

/** Rounded field with an animated gradient focus ring and floating label. */
public final class TextInput extends JTextField {

	private final String label;
	private final Anim focus = new Anim(this, 0, 12);

	public TextInput(String label, String value) {
		super(value);
		this.label = label;
		setOpaque(false);
		setForeground(Theme.TEXT);
		setCaretColor(Theme.A2);
		setSelectionColor(Theme.alpha(Theme.A1, 0.5f));
		setSelectedTextColor(Theme.TEXT);
		setFont(Theme.regular(14));
		setBorder(BorderFactory.createEmptyBorder(20, 14, 6, 14));
		addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) { focus.set(1); }
			public void focusLost(FocusEvent e) { focus.set(0); }
		});
	}

	@Override
	public Insets getInsets() {
		return new Insets(20, 14, 6, 14);
	}

	@Override
	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D) g0.create();
		Theme.hq(g);
		float w = getWidth(), h = getHeight(), f = focus.get();
		RoundRectangle2D.Float r = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 14, 14);
		g.setColor(Theme.mix(Theme.PANEL, Theme.PANEL_HI, f));
		g.fill(r);
		g.setColor(Theme.STROKE);
		g.draw(r);
		if (f > 0.01f) {
			g.setPaint(Theme.accent(0, 0, w, h));
			g.setComposite(java.awt.AlphaComposite.SrcOver.derive(f));
			g.setStroke(new java.awt.BasicStroke(1.5f));
			g.draw(r);
			g.setComposite(java.awt.AlphaComposite.SrcOver);
		}
		g.setFont(Theme.semibold(10));
		g.setColor(Theme.mix(Theme.MUTED, Theme.A2, f));
		g.drawString(label.toUpperCase(), 14, 15);
		g.dispose();
		super.paintComponent(g0);
	}
}
