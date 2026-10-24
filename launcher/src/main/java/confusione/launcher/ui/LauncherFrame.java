package confusione.launcher.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import confusione.launcher.core.ClientLocator;
import confusione.launcher.core.GameInstaller;
import confusione.launcher.core.GameLauncher;
import confusione.launcher.core.Progress;
import confusione.launcher.core.Settings;

/** Undecorated window: sidebar navigation, page host, and the launch controller. */
public final class LauncherFrame extends JFrame {

	public static final int W = 1000, H = 620;
	private static final int SIDEBAR = 76;

	private final Settings settings;
	final Root root = new Root();
	final HomePage home;
	final SettingsPage settingsPage;
	final ConsolePage console = new ConsolePage();
	final PageHost pages = new PageHost();
	private final NavButton navPlay = new NavButton(NavButton.Icon.PLAY, "Play");
	private final NavButton navSettings = new NavButton(NavButton.Icon.SETTINGS, "Settings");
	private final NavButton navConsole = new NavButton(NavButton.Icon.CONSOLE, "Console");
	private final NavButton min = new NavButton(NavButton.Icon.MINIMIZE, "Minimize");
	private final NavButton close = new NavButton(NavButton.Icon.CLOSE, "Close");
	private final Anim indicator = new Anim(root, 0, 12);
	private final Anim energy = new Anim(root, 0, 2);
	private volatile boolean working;

	public LauncherFrame(Settings s) {
		super("confusione");
		this.settings = s;
		setUndecorated(true);
		setSize(W, H);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		try {
			setShape(new RoundRectangle2D.Double(0, 0, W, H, 22, 22));
		} catch (UnsupportedOperationException ignored) {
			// no shaped windows on this platform: square corners
		}
		setIconImages(Icons.windowIcons());

		home = new HomePage(s.username);
		settingsPage = new SettingsPage(s);
		pages.addPage(home);
		pages.addPage(settingsPage);
		pages.addPage(console);

		root.setLayout(null);
		root.add(navPlay);
		root.add(navSettings);
		root.add(navConsole);
		root.add(min);
		root.add(close);
		root.add(pages);
		setContentPane(root);

		navPlay.setSelected(true);
		navPlay.onClick(new Runnable() { public void run() { go(home, 0); } });
		navSettings.onClick(new Runnable() { public void run() { go(settingsPage, 1); } });
		navConsole.onClick(new Runnable() { public void run() { go(console, 2); } });
		min.onClick(new Runnable() { public void run() { setState(ICONIFIED); } });
		close.onClick(new Runnable() { public void run() { settings.save(); System.exit(0); } });
		home.play.onClick(new Runnable() { public void run() { launch(); } });
		settingsPage.onChange(new Runnable() { public void run() { refreshClient(); } });

		Ambient.setReduceMotion(s.reduceMotion);
		Ambient.register(root);
		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) { Ambient.setFocused(true); }
			public void windowDeactivated(WindowEvent e) { Ambient.setFocused(false); }
			public void windowIconified(WindowEvent e) { Ambient.setFocused(false); }
		});

		MouseAdapter drag = new MouseAdapter() {
			private Point start;
			public void mousePressed(MouseEvent e) { start = e.getPoint(); }
			public void mouseDragged(MouseEvent e) {
				if (start != null) {
					Point p = getLocation();
					setLocation(p.x + e.getX() - start.x, p.y + e.getY() - start.y);
				}
			}
		};
		root.addMouseListener(drag);
		root.addMouseMotionListener(drag);
		refreshClient();
	}

	private void go(JComponent page, int index) {
		JComponent cur = pages.current();
		int curIndex = cur == home ? 0 : cur == settingsPage ? 1 : 2;
		navPlay.setSelected(index == 0);
		navSettings.setSelected(index == 1);
		navConsole.setSelected(index == 2);
		indicator.set(index);
		pages.show(page, index >= curIndex ? 1 : -1);
	}

	/** For screenshots / tests. */
	public void showPage(String name) {
		if ("settings".equals(name)) {
			go(settingsPage, 1);
		} else if ("console".equals(name)) {
			go(console, 2);
		} else {
			go(home, 0);
		}
	}

	private void refreshClient() {
		File jar = ClientLocator.find(settings.clientJar);
		final File repo = ClientLocator.findRepo();
		if (jar != null) {
			home.setClient("Client: " + jar.getName() + "  \u00b7  " + ClientLocator.tweakClass(jar), true, null);
		} else if (repo != null) {
			home.setClient("Client jar not built yet \u2014 click to build it (gradlew build)", false, new Runnable() {
				public void run() { build(repo); }
			});
		} else {
			home.setClient("Client jar not found \u2014 set its path in Settings", false, null);
		}
	}

	private Progress uiProgress() {
		return new Progress() {
			public void stage(final String text) {
				console.append("== " + text);
				SwingUtilities.invokeLater(new Runnable() { public void run() { home.play.setSub(text); home.setStatus(text, Theme.MUTED); } });
			}
			public void progress(final double v) {
				SwingUtilities.invokeLater(new Runnable() { public void run() { home.play.setProgress(v); } });
			}
			public void log(String line) {
				console.append(line);
			}
		};
	}

	private void setWorking(boolean w, String label) {
		working = w;
		Ambient.setBusy(w);
		energy.set(w ? 1 : 0);
		home.logo.setBusy(w);
		home.play.setState(w ? PlayButton.State.BUSY : PlayButton.State.IDLE, label);
		if (!w) {
			home.play.setSub("");
		}
	}

	private void build(final File repo) {
		if (working) {
			return;
		}
		setWorking(true, "BUILDING");
		final Progress p = uiProgress();
		new Thread(new Runnable() {
			public void run() {
				String result;
				Color c;
				try {
					String jh = settings.javaPath.isEmpty() ? System.getProperty("java.home") : new File(settings.javaPath).getParentFile().getParent();
					// a JRE's java.home may be <jdk>/jre; gradle needs the JDK root
					if (jh.endsWith(File.separator + "jre")) {
						jh = new File(jh).getParent();
					}
					int code = ClientLocator.build(repo, jh, p);
					result = code == 0 ? "Client built." : "Build failed (exit " + code + "), see Console.";
					c = code == 0 ? Theme.OK : Theme.ERROR;
				} catch (Exception e) {
					result = "Build failed: " + e.getMessage();
					c = Theme.ERROR;
					p.log(e.toString());
				}
				final String r = result;
				final Color cc = c;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setWorking(false, "PLAY");
						home.setStatus(r, cc);
						refreshClient();
					}
				});
			}
		}, "build").start();
	}

	private void launch() {
		if (working) {
			return;
		}
		String name = home.username.getText().trim();
		if (!name.matches("[A-Za-z0-9_]{3,16}")) {
			home.setStatus("Username: 3-16 letters, digits or _", Theme.ERROR);
			return;
		}
		settings.username = name;
		settings.save();
		final File jar = ClientLocator.find(settings.clientJar);
		if (jar == null) {
			home.setStatus("No client jar. Build it or set the path in Settings.", Theme.ERROR);
			return;
		}
		setWorking(true, "PREPARING");
		final Progress p = uiProgress();
		new Thread(new Runnable() {
			public void run() {
				try {
					GameInstaller.Result r = new GameInstaller(new File(settings.gameDir), p).install();
					List<String> cmd = GameLauncher.command(settings, r, jar);
					p.log("$ " + GameLauncher.toShell(cmd));
					p.stage("Starting Minecraft");
					final Process proc = GameLauncher.start(cmd, new File(settings.gameDir), p);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							working = true;
							Ambient.setBusy(false);
							energy.set(0.4f);
							home.logo.setBusy(false);
							home.play.setState(PlayButton.State.RUNNING, "RUNNING");
							home.play.setSub("");
							home.setStatus("Minecraft is running as " + settings.username + ".", Theme.OK);
							if (!settings.keepOpen) {
								setState(ICONIFIED);
							}
						}
					});
					final int code = proc.waitFor();
					p.log("Game exited with code " + code);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							setWorking(false, "PLAY");
							home.setStatus(code == 0 ? "Game closed." : "Game exited with code " + code + " \u2014 see Console.", code == 0 ? Theme.MUTED : Theme.ERROR);
							if (getState() == ICONIFIED) {
								setState(NORMAL);
							}
						}
					});
				} catch (final Exception e) {
					p.log("ERROR: " + e);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							setWorking(false, "PLAY");
							home.setStatus("Failed: " + e.getMessage(), Theme.ERROR);
						}
					});
				}
			}
		}, "launch").start();
	}

	/** Demo state for screenshots. */
	public void demoProgress(double v, String stage) {
		setWorking(true, "PREPARING");
		home.play.setProgress(v);
		home.play.setSub(stage);
		home.setStatus(stage, Theme.MUTED);
	}

	final class Root extends JComponent {
		private final Background bg = new Background();

		Root() {
			setOpaque(true);
		}

		@Override
		public void doLayout() {
			int y = 112;
			for (NavButton b : new NavButton[] { navPlay, navSettings, navConsole }) {
				b.setBounds(14, y, 48, 48);
				y += 56;
			}
			close.setBounds(getWidth() - 40, 10, 28, 28);
			min.setBounds(getWidth() - 70, 10, 28, 28);
			pages.setBounds(SIDEBAR, 0, getWidth() - SIDEBAR, getHeight());
		}

		@Override
		protected void paintComponent(Graphics g0) {
			Graphics2D g = (Graphics2D) g0.create();
			bg.paint(g, getWidth(), getHeight(), Ambient.time(), energy.get());
			Theme.hq(g);
			// sidebar glass
			g.setColor(new Color(0, 0, 0, 70));
			g.fillRect(0, 0, SIDEBAR, getHeight());
			g.setColor(Theme.STROKE);
			g.drawLine(SIDEBAR, 0, SIDEBAR, getHeight());
			// mini mark
			g.drawImage(Icons.mark(40), 18, 24, null);
			// sliding selection bar
			float iy = 112 + indicator.get() * 56 + 14;
			g.setPaint(Theme.accent(0, iy, 0, iy + 20));
			g.fill(new RoundRectangle2D.Float(0, iy, 3.5f, 20, 3, 3));
			g.setFont(Theme.regular(10));
			g.setColor(Theme.alpha(Theme.MUTED, 0.7f));
			g.drawString("v0.1", 26, getHeight() - 18);
			g.dispose();
		}
	}
}
