package me.eldodebug.soar.management.music;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.notification.NotificationType;

/**
 * One facade over the two sources. Nothing here runs while the Music Player mod is
 * off: no thread, no timer, no network. All slow work happens on one daemon worker,
 * the render thread only reads the volatile {@link NowPlaying} snapshot.
 */
public final class MusicManager {

	public enum Source { LOCAL, SPOTIFY }

	private static final long POLL_MS = 3000;

	private final File musicDir;
	private final LocalPlayer local;
	private final SpotifyClient spotify;
	private volatile NowPlaying now = NowPlaying.NOTHING;
	private volatile Source source = Source.LOCAL;
	private volatile boolean active;
	private ScheduledExecutorService worker;
	private ScheduledFuture<?> poll;
	private long backoffUntil;

	public MusicManager(File musicDir) {
		this.musicDir = musicDir;
		this.local = new LocalPlayer(new LocalPlayer.Listener() {
			public void changed(NowPlaying np) {
				if (source == Source.LOCAL) {
					now = np;
				}
			}

			public void error(String message) {
				toast(message, NotificationType.WARNING);
			}
		});
		this.spotify = new SpotifyClient(new File(musicDir, "spotify.properties"));
		this.spotify.setAuthCallback(new SpotifyClient.AuthCallback() {
			public void done(boolean ok, String message) {
				toast(message, ok ? NotificationType.SUCCESS : NotificationType.ERROR);
				if (ok) {
					pollSoon();
				}
			}
		});
	}

	public NowPlaying getNowPlaying() {
		return now;
	}

	public Source getSource() {
		return source;
	}

	public LocalPlayer getLocal() {
		return local;
	}

	public SpotifyClient getSpotify() {
		return spotify;
	}

	public File getMusicDir() {
		return musicDir;
	}

	// ---- lifecycle (driven by MusicPlayerMod) ------------------------------------

	public synchronized void activate(Source src) {
		active = true;
		if (worker == null) {
			worker = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r, "CONFUSION3H-music-worker");
					t.setDaemon(true);
					return t;
				}
			});
		}
		applySource(src, true); // always (re)scan / start polling on enable
	}

	public synchronized void deactivate() {
		active = false;
		cancelPoll();
		spotify.cancelAuthorization();
		if (worker != null) {
			worker.shutdownNow();
			worker = null;
		}
		// stop() joins the playback thread; don't hold up the caller
		Thread t = new Thread(new Runnable() {
			public void run() {
				local.stop();
			}
		}, "CONFUSION3H-music-stop");
		t.setDaemon(true);
		t.start();
		now = NowPlaying.NOTHING;
	}

	public synchronized void setSource(Source src) {
		applySource(src, false);
	}

	private void applySource(Source src, boolean force) {
		// before: the LOCAL default short-circuited here on enable, so the folder was
		// never scanned and "play" found an empty playlist
		if (!force && src == source && (src != Source.SPOTIFY || poll != null)) {
			return;
		}
		source = src;
		now = NowPlaying.NOTHING;
		cancelPoll();
		if (src == Source.SPOTIFY) {
			async(new Runnable() {
				public void run() {
					local.stop();
				}
			});
			if (spotify.isAuthorized()) {
				poll = worker.scheduleWithFixedDelay(new Runnable() {
					public void run() {
						pollOnce();
					}
				}, 0, POLL_MS, TimeUnit.MILLISECONDS);
			}
		} else {
			async(new Runnable() {
				public void run() {
					local.scan(musicDir);
				}
			});
		}
	}

	private void cancelPoll() {
		if (poll != null) {
			poll.cancel(false);
			poll = null;
		}
	}

	private void pollOnce() {
		if (!active || source != Source.SPOTIFY || System.currentTimeMillis() < backoffUntil) {
			return;
		}
		try {
			now = spotify.fetchState();
		} catch (IOException e) {
			backoffUntil = System.currentTimeMillis() + 15000; // don't hammer the API on errors
			now = NowPlaying.NOTHING;
		}
	}

	private void pollSoon() {
		final ScheduledExecutorService w = worker;
		if (w != null && source == Source.SPOTIFY) {
			synchronized (this) {
				if (poll == null && spotify.isAuthorized()) {
					poll = w.scheduleWithFixedDelay(new Runnable() {
						public void run() {
							pollOnce();
						}
					}, 0, POLL_MS, TimeUnit.MILLISECONDS);
					return;
				}
			}
			w.schedule(new Runnable() {
				public void run() {
					backoffUntil = 0;
					pollOnce();
				}
			}, 400, TimeUnit.MILLISECONDS);
		}
	}

	// ---- controls -----------------------------------------------------------------

	public interface SpotifyAction {
		void run() throws IOException;
	}

	private void spotifyAction(final SpotifyAction a) {
		async(new Runnable() {
			public void run() {
				try {
					a.run();
					pollSoon();
				} catch (IOException e) {
					toast(e.getMessage(), NotificationType.WARNING);
				}
			}
		});
	}

	public void togglePause() {
		if (source == Source.SPOTIFY) {
			final boolean playing = now.playing;
			spotifyAction(new SpotifyAction() {
				public void run() throws IOException {
					if (playing) {
						spotify.pause();
					} else {
						spotify.play();
					}
				}
			});
		} else {
			async(new Runnable() {
				public void run() {
					local.togglePause();
				}
			});
		}
	}

	public void play(final boolean wantPlaying) {
		if (source == Source.SPOTIFY) {
			spotifyAction(new SpotifyAction() {
				public void run() throws IOException {
					if (wantPlaying) {
						spotify.play();
					} else {
						spotify.pause();
					}
				}
			});
		} else if (local.isPlaying() != wantPlaying) {
			togglePause();
		}
	}

	public void next() {
		if (source == Source.SPOTIFY) {
			spotifyAction(new SpotifyAction() {
				public void run() throws IOException {
					spotify.next();
				}
			});
		} else {
			async(new Runnable() {
				public void run() {
					local.next();
				}
			});
		}
	}

	public void previous() {
		if (source == Source.SPOTIFY) {
			spotifyAction(new SpotifyAction() {
				public void run() throws IOException {
					spotify.previous();
				}
			});
		} else {
			async(new Runnable() {
				public void run() {
					local.previous();
				}
			});
		}
	}

	/** Local volume is applied immediately; Spotify volume only on explicit command. */
	public void setLocalVolume(float v) {
		local.setVolume(v);
	}

	public void setSpotifyVolume(final int percent) {
		spotifyAction(new SpotifyAction() {
			public void run() throws IOException {
				spotify.setVolume(percent);
			}
		});
	}

	public void setShuffle(boolean on) {
		local.setShuffle(on);
	}

	public void connectSpotify(String clientId) {
		spotify.setClientId(clientId);
		try {
			String url = spotify.beginAuthorization();
			openBrowser(url);
			toast("Approve access in your browser (" + SpotifyClient.REDIRECT_URI + ")", NotificationType.INFO);
		} catch (IOException e) {
			toast(e.getMessage(), NotificationType.ERROR);
		}
	}

	public void disconnectSpotify() {
		spotify.logout();
		synchronized (this) {
			cancelPoll();
		}
		now = source == Source.SPOTIFY ? NowPlaying.NOTHING : now;
		toast("Disconnected from Spotify", NotificationType.INFO);
	}

	public void rescan() {
		async(new Runnable() {
			public void run() {
				int n = local.scan(musicDir);
				toast(n + " tracks in " + musicDir.getName(), NotificationType.MUSIC);
			}
		});
	}

	private void async(Runnable r) {
		ScheduledExecutorService w = worker;
		if (w == null) {
			toast("Enable the Music Player mod first", NotificationType.INFO);
			return;
		}
		w.execute(r);
	}

	static void openBrowser(String url) {
		try {
			if (java.awt.Desktop.isDesktopSupported()) {
				java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
				return;
			}
		} catch (Exception ignored) {
		}
		try {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("mac")) {
				Runtime.getRuntime().exec(new String[] { "open", url });
			} else if (os.contains("win")) {
				Runtime.getRuntime().exec(new String[] { "rundll32", "url.dll,FileProtocolHandler", url });
			} else {
				Runtime.getRuntime().exec(new String[] { "xdg-open", url });
			}
		} catch (IOException e) {
			me.eldodebug.soar.logger.GlideLogger.info("Open this URL to connect Spotify: " + url);
		}
	}

	private void toast(String msg, NotificationType type) {
		Glide g = Glide.getInstance();
		if (g != null && g.getNotificationManager() != null) {
			g.getNotificationManager().post("Music", msg, type);
		}
	}
}
