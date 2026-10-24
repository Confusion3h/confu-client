package confusione.launcher.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** Launcher + game output. Appends are batched onto the EDT. */
public final class ConsolePage extends JComponent {

	private static final int MAX_CHARS = 400_000;
	private final JTextArea area = new JTextArea();
	private final JScrollPane scroll;
	private final StringBuilder pending = new StringBuilder();
	private boolean flushQueued;

	public ConsolePage() {
		setOpaque(false);
		setLayout(null);
		area.setEditable(false);
		area.setOpaque(false);
		area.setForeground(new Color(0xCFCFCF));
		area.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
		area.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
		area.setLineWrap(false);
		scroll = new JScrollPane(area);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setUI(new ThinBar());
		scroll.getHorizontalScrollBar().setUI(new ThinBar());
		scroll.getVerticalScrollBar().setOpaque(false);
		scroll.getHorizontalScrollBar().setOpaque(false);
		scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
		add(scroll);
	}

	/** Thread-safe. */
	public void append(String line) {
		synchronized (pending) {
			pending.append(line).append('\n');
			if (flushQueued) {
				return;
			}
			flushQueued = true;
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String chunk;
				synchronized (pending) {
					chunk = pending.toString();
					pending.setLength(0);
					flushQueued = false;
				}
				area.append(chunk);
				int len = area.getDocument().getLength();
				if (len > MAX_CHARS) {
					area.replaceRange("", 0, len - MAX_CHARS);
				}
				area.setCaretPosition(area.getDocument().getLength());
			}
		});
	}

	public String text() {
		return area.getText();
	}

	@Override
	public void doLayout() {
		scroll.setBounds(48, 110, getWidth() - 96, getHeight() - 150);
	}

	@Override
	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D) g0.create();
		Theme.hq(g);
		g.setFont(Theme.semibold(26));
		g.setColor(Theme.TEXT);
		g.drawString("Console", 48, 70);
		g.setFont(Theme.regular(13));
		g.setColor(Theme.MUTED);
		g.drawString("Setup progress and game output.", 48, 92);
		g.setColor(new Color(0, 0, 0, 90));
		g.fill(new RoundRectangle2D.Float(44, 106, getWidth() - 88, getHeight() - 142, 16, 16));
		g.setColor(Theme.STROKE);
		g.draw(new RoundRectangle2D.Float(44, 106, getWidth() - 88, getHeight() - 142, 16, 16));
		g.dispose();
	}

	private static final class ThinBar extends BasicScrollBarUI {
		@Override
		protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
		}

		@Override
		protected void paintThumb(Graphics g0, JComponent c, Rectangle r) {
			Graphics2D g = (Graphics2D) g0.create();
			Theme.hq(g);
			g.setColor(Theme.alpha(Theme.TEXT, isThumbRollover() ? 0.35f : 0.18f));
			g.fill(new RoundRectangle2D.Float(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 6, 6));
			g.dispose();
		}

		@Override
		protected JButton createDecreaseButton(int o) {
			return zero();
		}

		@Override
		protected JButton createIncreaseButton(int o) {
			return zero();
		}

		private static JButton zero() {
			JButton b = new JButton();
			b.setPreferredSize(new Dimension(0, 0));
			return b;
		}
	}
}
