package confusione.launcher.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

/** Wordmark, username, play button, client status and a "what's new" column. */
public final class HomePage extends JComponent implements PageHost.Entrance {

	private static final String[][] NEWS = {
		{ "FPS mode", "On by default. Blur, glow, particles and click effects off; one toggle in Settings > General." },
		{ "Leaner core", "Allocation-free event bus and NanoVG draw calls, cheaper HUD blur." },
		{ "CONFUSION3H launcher", "Sets up 1.8.9 from Mojang's servers and starts the client for you." },
		{ "Credits", "CONFUSION3H. Based on Glide Client and Soar Client by EldoDebug · GPL-3.0." },
	};

	final LogoMark logo = new LogoMark();
	final TextInput username;
	final PlayButton play = new PlayButton();
	private final Anim enter = new Anim(this, 1, 5);
	private String status = "";
	private Color statusColor = Theme.MUTED;
	private String clientInfo = "";
	private boolean clientOk;
	private Runnable buildAction;
	private final Anim linkHover = new Anim(this, 0, 14);
	private boolean microsoft;
	private String accountName = "";
	private String deviceUrl = "";
	private String deviceCode = "";
	private boolean polling;
	private final Rectangle offlineChip = new Rectangle();
	private final Rectangle msChip = new Rectangle();
	private final Rectangle actionChip = new Rectangle();
	private Runnable onOffline, onMicrosoft, onAccountAction;

	public HomePage(String user) {
		setOpaque(false);
		setLayout(null);
		username = new TextInput("Username (offline)", user);
		add(logo);
		add(username);
		add(play);
		MouseAdapter link = new MouseAdapter() {
			public void mouseMoved(MouseEvent e) {
				boolean in = hit(e);
				linkHover.set(!clientOk && buildAction != null && linkBounds().contains(e.getPoint()) ? 1 : 0);
				setCursor(Cursor.getPredefinedCursor(in ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
			}
			public void mouseReleased(MouseEvent e) {
				if (offlineChip.contains(e.getPoint()) && onOffline != null) {
					onOffline.run();
					return;
				}
				if (msChip.contains(e.getPoint()) && onMicrosoft != null) {
					onMicrosoft.run();
					return;
				}
				if (microsoft && actionChip.width > 0 && actionChip.contains(e.getPoint()) && onAccountAction != null) {
					onAccountAction.run();
					return;
				}
				if (!clientOk && buildAction != null && linkBounds().contains(e.getPoint())) {
					buildAction.run();
				}
			}
			private boolean hit(MouseEvent e) {
				if (offlineChip.contains(e.getPoint()) || msChip.contains(e.getPoint())) {
					return true;
				}
				if (microsoft && actionChip.width > 0 && actionChip.contains(e.getPoint())) {
					return true;
				}
				return !clientOk && buildAction != null && linkBounds().contains(e.getPoint());
			}
		};
		addMouseListener(link);
		addMouseMotionListener(link);
	}

	public void enter() {
		enter.snap(0);
		enter.set(1);
	}

	public void setStatus(String s, Color c) {
		status = s == null ? "" : s;
		statusColor = c;
		repaint();
	}

	public void onAccount(Runnable offline, Runnable microsoft, Runnable action) {
		onOffline = offline;
		onMicrosoft = microsoft;
		onAccountAction = action;
	}

	/** @param name profile name, empty when signed out */
	public void setAccount(boolean microsoft, String name, boolean polling, String url, String code) {
		this.microsoft = microsoft;
		this.accountName = name == null ? "" : name;
		this.polling = polling;
		this.deviceUrl = url == null ? "" : url;
		this.deviceCode = code == null ? "" : code;
		username.setVisible(!microsoft);
		revalidate();
		repaint();
	}

	public void setClient(String info, boolean ok, Runnable build) {
		clientInfo = info;
		clientOk = ok;
		buildAction = build;
		repaint();
	}

	private java.awt.Rectangle linkBounds() {
		return new java.awt.Rectangle(48, getHeight() - 58, 420, 24);
	}

	@Override
	public void doLayout() {
		logo.setBounds(40, 54, 92, 92);
		username.setVisible(!microsoft);
		username.setBounds(48, 268, 320, 52);
		play.setBounds(42, 328, Math.min(360, Math.max(280, getWidth() - 420)), 100);
		offlineChip.setBounds(48, 224, 84, 26);
		msChip.setBounds(140, 224, 108, 26);
		if (microsoft) {
			String label = accountActionLabel();
			actionChip.setBounds(48, 276, Math.max(96, label.length() * 8 + 28), 30);
		} else {
			actionChip.setBounds(0, 0, 0, 0);
		}
	}

	@Override
	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D) g0.create();
		Theme.hq(g);
		int w = getWidth(), h = getHeight();
		float e = enter.get();

		// wordmark
		g.setFont(Theme.semibold(46));
		g.setPaint(Theme.accent(150, 0, 420, 0));
		g.drawString("CONFUSION3H", 146, 116);
		g.setFont(Theme.regular(14));
		g.setColor(Theme.MUTED);
		g.drawString("Minecraft 1.8.9  \u00b7  FPS-first client", 150, 140);

		g.setFont(Theme.semibold(26));
		g.setColor(Theme.TEXT);
		g.drawString("Ready when you are.", 48, 212);
		chip(g, offlineChip, "Offline", !microsoft);
		chip(g, msChip, "Microsoft", microsoft);
		if (microsoft) {
			String action = accountActionLabel();
			chip(g, actionChip, action, true, accountName.isEmpty());
			g.setFont(Theme.regular(13));
			g.setColor(Theme.TEXT);
			String detail;
			if (polling) {
				detail = deviceCode.isEmpty() ? "Waiting for the browser" : deviceCode;
			} else if (accountName.isEmpty()) {
				detail = "Not signed in";
			} else {
				detail = accountName;
			}
			g.drawString(ellipsize(g.getFontMetrics(), detail, 220), actionChip.x + actionChip.width + 12, actionChip.y + 20);
			if (polling && !deviceUrl.isEmpty()) {
				g.setFont(Theme.regular(11));
				g.setColor(Theme.MUTED);
				g.drawString(ellipsize(g.getFontMetrics(), deviceUrl, 300), 48, 322);
			}
		}

		// status line under the button
		if (!status.isEmpty()) {
			g.setFont(Theme.regular(12));
			g.setColor(statusColor);
			g.drawString(ellipsize(g.getFontMetrics(), status, Math.max(200, getWidth() - 420)), 48, 452);
		}

		// client chip
		g.setFont(Theme.regular(12));
		float lx = 48, ly = h - 42;
		g.setColor(clientOk ? Theme.OK : Theme.A3);
		g.fill(new Ellipse2D.Float(lx, ly - 8, 8, 8));
		g.setColor(clientOk ? Theme.MUTED : Theme.mix(Theme.TEXT, Theme.A3, linkHover.get()));
		g.drawString(ellipsize(g.getFontMetrics(), clientInfo, 400), lx + 16, ly);

		// news column with staggered entrance
		float cx = w - 330, cy = 72;
		g.setFont(Theme.semibold(11));
		g.setColor(Theme.MUTED);
		g.drawString("WHAT'S NEW", cx, cy - 12);
		for (int i = 0; i < NEWS.length; i++) {
			float k = clamp((e - i * 0.15f) / 0.7f);
			float k2 = 1 - (1 - k) * (1 - k);
			Graphics2D gc = (Graphics2D) g.create();
			Theme.modulate(gc, k2);
			gc.translate(30 * (1 - k2), 0);
			float y = cy + i * 108;
			RoundRectangle2D.Float card = new RoundRectangle2D.Float(cx, y, 290, 94, 18, 18);
			gc.setColor(Theme.PANEL);
			gc.fill(card);
			gc.setColor(Theme.STROKE);
			gc.draw(card);
			gc.setPaint(Theme.accent(cx, y, cx + 4, y + 94));
			gc.fill(new RoundRectangle2D.Float(cx + 14, y + 18, 3, 18, 3, 3));
			gc.setFont(Theme.semibold(14));
			gc.setColor(Theme.TEXT);
			gc.drawString(NEWS[i][0], cx + 26, y + 32);
			gc.setFont(Theme.regular(12));
			gc.setColor(Theme.MUTED);
			wrap(gc, NEWS[i][1], cx + 26, y + 52, 250, 16);
			gc.dispose();
		}
		g.dispose();
	}

	private static float clamp(float v) {
		return Math.max(0, Math.min(1, v));
	}

	/** True when a press on the page itself hits a control rather than empty background. */
	public boolean hitsControl(java.awt.Point p) {
		if (offlineChip.contains(p) || msChip.contains(p)) {
			return true;
		}
		if (microsoft && actionChip.width > 0 && actionChip.contains(p)) {
			return true;
		}
		return !clientOk && buildAction != null && linkBounds().contains(p);
	}

	private String accountActionLabel() {
		if (!polling) {
			return accountName.isEmpty() ? "Sign in" : "Sign out";
		}
		return deviceCode.isEmpty() ? "Use a code" : "Cancel";
	}

	private static void chip(Graphics2D g, Rectangle r, String text, boolean on) {
		chip(g, r, text, on, false);
	}

	private static void chip(Graphics2D g, Rectangle r, String text, boolean on, boolean accent) {
		if (r.width <= 0) {
			return;
		}
		RoundRectangle2D.Float rr = new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, r.height, r.height);
		if (on && accent) {
			g.setPaint(new java.awt.GradientPaint(r.x, r.y, Theme.ACCENT_HI, r.x, r.y + r.height, Theme.ACCENT));
			g.fill(rr);
			g.setColor(Theme.ON_ACCENT);
		} else if (on) {
			g.setPaint(Theme.accent(r.x, r.y, r.x + r.width, r.y));
			g.fill(rr);
			g.setColor(Theme.ON_ACCENT);
		} else {
			g.setColor(Theme.PANEL);
			g.fill(rr);
			g.setColor(Theme.STROKE);
			g.draw(rr);
			g.setColor(Theme.TEXT);
		}
		g.setFont(Theme.semibold(12));
		FontMetrics fm = g.getFontMetrics();
		g.drawString(text, r.x + (r.width - fm.stringWidth(text)) / 2f, r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2f - 1);
	}

	static String ellipsize(FontMetrics fm, String s, int max) {
		if (fm.stringWidth(s) <= max) {
			return s;
		}
		while (s.length() > 1 && fm.stringWidth(s + "\u2026") > max) {
			s = s.substring(0, s.length() - 1);
		}
		return s + "\u2026";
	}

	static void wrap(Graphics2D g, String text, float x, float y, int max, int lh) {
		FontMetrics fm = g.getFontMetrics();
		StringBuilder line = new StringBuilder();
		for (String word : text.split(" ")) {
			if (line.length() > 0 && fm.stringWidth(line + " " + word) > max) {
				g.drawString(line.toString(), x, y);
				y += lh;
				line.setLength(0);
			}
			if (line.length() > 0) {
				line.append(' ');
			}
			line.append(word);
		}
		g.drawString(line.toString(), x, y);
	}
}
