package confusione.launcher;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import confusione.launcher.core.ClientLocator;
import confusione.launcher.core.GameInstaller;
import confusione.launcher.core.GameLauncher;
import confusione.launcher.core.MicrosoftAuth;
import confusione.launcher.core.Progress;
import confusione.launcher.core.Settings;
import confusione.launcher.ui.LauncherFrame;

/**
 * CONFUSION3H launcher.
 *   (no args)                 open the UI
 *   --setup-only              download/verify 1.8.9 files, no launch
 *   --dry-run                 setup, then print the launch command instead of running it
 *   --launch                  setup and launch without UI
 *   --screenshot FILE [--page home|settings|console] [--progress 0.4]   render UI to PNG
 *   --gamedir DIR --username NAME --client JAR   overrides (not saved)
 */
public final class Main {

	public static void main(String[] args) throws Exception {
		final Settings s = Settings.load();
		String mode = "ui", shot = null, page = "home";
		double demo = -2;
		for (int i = 0; i < args.length; i++) {
			String a = args[i];
			if (a.equals("--setup-only") || a.equals("--dry-run") || a.equals("--launch")) {
				mode = a.substring(2);
			} else if (a.equals("--screenshot")) {
				mode = "screenshot";
				shot = args[++i];
			} else if (a.equals("--page")) {
				page = args[++i];
			} else if (a.equals("--progress")) {
				demo = Double.parseDouble(args[++i]);
			} else if (a.equals("--gamedir")) {
				s.gameDir = args[++i];
			} else if (a.equals("--username")) {
				s.username = args[++i];
			} else if (a.equals("--client")) {
				s.clientJar = args[++i];
			} else {
				System.err.println("Unknown argument: " + a);
				System.exit(2);
			}
		}

		if (!mode.equals("ui") && !mode.equals("screenshot")) {
			System.exit(cli(s, mode));
		}

		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("sun.java2d.opengl", System.getProperty("sun.java2d.opengl", "false"));
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

		final String fShot = shot, fPage = page;
		final double fDemo = demo;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final LauncherFrame f = new LauncherFrame(s);
				f.setVisible(true);
				if (fShot == null) {
					return;
				}
				f.showPage(fPage);
				if (fDemo > -2) {
					f.demoProgress(fDemo, "Assets (412 of 1205 to fetch)");
				}
				// let transitions settle, then paint the content pane into a PNG
				new javax.swing.Timer(1500, new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						try {
							BufferedImage img = new BufferedImage(f.getWidth(), f.getHeight(), BufferedImage.TYPE_INT_RGB);
							f.getContentPane().paint(img.getGraphics());
							ImageIO.write(img, "png", new File(fShot));
							System.out.println("Wrote " + fShot);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						System.exit(0);
					}
				}).start();
			}
		});
	}

	private static int cli(Settings s, String mode) throws Exception {
		File jar = ClientLocator.find(s.clientJar);
		if (jar == null && !mode.equals("setup-only")) {
			System.err.println("Client jar not found (build it with ./gradlew build, or pass --client).");
			return 1;
		}
		MicrosoftAuth.Session session = null;
		if (!mode.equals("setup-only") && s.microsoft) {
			session = MicrosoftAuth.ensure(s.msClientId, Progress.CONSOLE);
		}
		GameInstaller.Result r = new GameInstaller(new File(s.gameDir), Progress.CONSOLE).install();
		if (mode.equals("setup-only")) {
			return 0;
		}
		List<String> cmd = GameLauncher.command(s, r, jar, session);
		if (mode.equals("dry-run")) {
			System.out.println("Client jar : " + jar);
			System.out.println("Tweak class: " + ClientLocator.tweakClass(jar));
			System.out.println("Classpath  : " + (r.classpath.size() + 2) + " entries");
			for (String c : cmd) {
				if (c.contains(File.pathSeparator) && c.length() > 200) {
					for (String e : c.split(File.pathSeparator)) {
						System.out.println("    " + e + (new File(e).isFile() ? "" : "   [MISSING]"));
					}
				}
			}
			System.out.println(GameLauncher.toShell(cmd));
			return 0;
		}
		Process p = GameLauncher.start(cmd, new File(s.gameDir), Progress.CONSOLE);
		return p.waitFor();
	}
}
