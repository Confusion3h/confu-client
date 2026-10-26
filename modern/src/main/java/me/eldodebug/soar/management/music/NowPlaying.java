package me.eldodebug.soar.management.music;

/**
 * Immutable snapshot published by a player. The HUD extrapolates the position from
 * {@link #sampledAt} so it never has to ask the player (or the network) per frame.
 */
public final class NowPlaying {

	public static final NowPlaying NOTHING = new NowPlaying(null, null, 0, -1, false, 0);

	public final String title;
	public final String artist;
	public final long positionMs;
	public final long durationMs;
	public final boolean playing;
	public final long sampledAt;

	public NowPlaying(String title, String artist, long positionMs, long durationMs, boolean playing, long sampledAt) {
		this.title = title;
		this.artist = artist;
		this.positionMs = positionMs;
		this.durationMs = durationMs;
		this.playing = playing;
		this.sampledAt = sampledAt;
	}

	public boolean isEmpty() {
		return title == null;
	}

	public long positionNow() {
		long p = playing ? positionMs + (System.currentTimeMillis() - sampledAt) : positionMs;
		return durationMs > 0 ? Math.min(p, durationMs) : p;
	}
}
