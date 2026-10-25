package confusione.launcher.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import confusione.launcher.core.Settings;

/** Paths, memory and launcher behavior; every change is saved immediately. */
public final class SettingsPage extends JComponent implements PageHost.Entrance {

	private final Settings s;
	private final TextInput gameDir, javaPath, clientJar;
	private final Slider ram;
	private final Toggle reduceMotion, keepOpen;
	private final Anim enter = new Anim(this, 1, 6);
	private Runnable onChange;

	public SettingsPage(final Settings s) {
		this.s = s;
		setOpaque(false);
		setLayout(null);
		gameDir = field("Game directory", s.gameDir);
		javaPath = field("Java 8 executable (empty = this JVM)", s.javaPath);
		clientJar = field("Client jar (empty = auto-detect build/libs)", s.clientJar);
		long maxMb = Math.max(2048, Runtime.getRuntime().maxMemory() / 1024 / 1024 * 4);
		ram = new Slider("Memory", 1024, (int) Math.min(16384, Math.max(4096, maxMb)), 512, s.ramMb);
		ram.setListener(new Slider.Listener() {
			public void changed(int v) { s.ramMb = v; SettingsPage.this.changed(); }
		});
		reduceMotion = new Toggle("Reduce motion", "Freeze the animated background and logo", s.reduceMotion);
		reduceMotion.setListener(new Toggle.Listener() {
			public void changed(boolean on) { s.reduceMotion = on; Ambient.setReduceMotion(on); SettingsPage.this.changed(); }
		});
		keepOpen = new Toggle("Keep launcher open", "Otherwise it minimizes while the game runs", s.keepOpen);
		keepOpen.setListener(new Toggle.Listener() {
			public void changed(boolean on) { s.keepOpen = on; SettingsPage.this.changed(); }
		});
		add(ram);
		add(reduceMotion);
		add(keepOpen);
	}

	public void onChange(Runnable r) {
		onChange = r;
	}

	private TextInput field(String label, String value) {
		final TextInput t = new TextInput(label, value);
		t.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) { sync(); }
			public void removeUpdate(DocumentEvent e) { sync(); }
			public void changedUpdate(DocumentEvent e) { sync(); }
		});
		add(t);
		return t;
	}

	private void sync() {
		s.gameDir = gameDir.getText().trim();
		s.javaPath = javaPath.getText().trim();
		s.clientJar = clientJar.getText().trim();
		changed();
	}

	private void changed() {
		s.save();
		if (onChange != null) {
			onChange.run();
		}
	}

	public void enter() {
		enter.snap(0);
		enter.set(1);
	}

	@Override
	public void doLayout() {
		int x = 48, w = Math.min(560, getWidth() - 96);
		float e = enter.get();
		JComponent[] rows = { gameDir, javaPath, clientJar, ram, reduceMotion, keepOpen };
		int y = 110;
		for (int i = 0; i < rows.length; i++) {
			int h = i < 3 ? 52 : 56;
			int off = Math.round(20 * (1 - clamp((e - i * 0.08f) / 0.6f)));
			rows[i].setBounds(x + off, y, w, h);
			y += h + 12;
		}
	}

	private static float clamp(float v) {
		return Math.max(0, Math.min(1, v));
	}

	@Override
	protected void paintComponent(Graphics g0) {
		if (enter.get() < 1) {
			doLayout(); // drives the staggered slide-in
		}
		Graphics2D g = (Graphics2D) g0.create();
		Theme.hq(g);
		g.setFont(Theme.semibold(26));
		g.setColor(Theme.TEXT);
		g.drawString("Settings", 48, 70);
		g.setFont(Theme.regular(13));
		g.setColor(Theme.MUTED);
		g.drawString("Saved as you type. The Microsoft refresh token is stored separately, not here.", 48, 92);
		g.dispose();
	}
}
