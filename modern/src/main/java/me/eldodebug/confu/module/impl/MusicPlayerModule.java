package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.KeybindSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.util.Keys;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.gui.GuiAddTrack;
import me.eldodebug.soar.management.music.LinkGrabber;
import me.eldodebug.soar.management.music.MusicManager;
import me.eldodebug.soar.management.music.NowPlaying;
import me.eldodebug.soar.management.nanovg.font.Font;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

/**
 * 1.8.9 MusicPlayerMod (local source): plays MP3/OGG/WAV from .confu/minecraft/glide/music,
 * the same folder as 1.8.9, with the same HUD, keys and "add a link" download.
 * Off by default; while off the music manager owns no threads.
 */
public final class MusicPlayerModule extends NvgHudModule {

	private static final float W = 150, H = 38;

	private final NumberSetting volume = number("volume_pct", "tt:VOLUME", 60, 0, 100, 1);
	private final BooleanSetting shuffle = bool("shuffle", "tt:SHUFFLE", false);
	private final BooleanSetting progress = bool("progress", "tt:PROGRESS_BAR", true);
	private final BooleanSetting icon = bool("icon", "tt:ICON", true);
	private final KeybindSetting playPauseKey = extraKey("play_pause_key", "tt:PLAY_PAUSE_KEY", -1);
	private final KeybindSetting nextKey = extraKey("next_key2", "tt:NEXT_TRACK_KEY", -1);
	private final KeybindSetting prevKey = extraKey("prev_key", "tt:PREVIOUS_TRACK_KEY", -1);
	private final BooleanSetting addLink = bool("add_link", "tt:ADD_TRACK", false);

	private int lastVolume = -1;
	private boolean lastShuffle;
	private final boolean[] wasDown = new boolean[3];

	public MusicPlayerModule() {
		super("music_player", Category.HUD, "tt:MUSIC_PLAYER", "tt:MUSIC_PLAYER_DESCRIPTION", 0.5F, 0F);
	}

	private MusicManager music() {
		return Glide.getInstance().getMusicManager();
	}

	@Override
	protected void onEnable() {
		music().activate(MusicManager.Source.LOCAL);
		sync(true);
	}

	@Override
	protected void onDisable() {
		music().deactivate();
	}

	private void sync(boolean force) {
		int vol = volume.getInt();
		if (force || vol != lastVolume) {
			lastVolume = vol;
			music().setLocalVolume(vol / 100f);
		}
		boolean sh = shuffle.enabled();
		if (force || sh != lastShuffle) {
			lastShuffle = sh;
			music().setShuffle(sh);
		}
	}

	@Override
	public void onTick() {
		if (addLink.get()) {
			addLink.set(false);
			mc.setScreen(new GuiAddTrack(mc.currentScreen));
		}
		sync(false);
		KeybindSetting[] keys = {playPauseKey, nextKey, prevKey};
		for (int i = 0; i < 3; i++) {
			boolean down = mc.currentScreen == null && keys[i].bound() && Keys.isDown(keys[i].code());
			if (down && !wasDown[i]) {
				if (i == 0) {
					music().togglePause();
				} else if (i == 1) {
					music().next();
				} else {
					music().previous();
				}
			}
			wasDown[i] = down;
		}
	}

	@Override
	protected boolean visible() {
		return !music().getNowPlaying().isEmpty() || LinkGrabber.active();
	}

	@Override
	protected void drawNanoVG() {
		NowPlaying np = music().getNowPlaying();
		boolean grabbing = LinkGrabber.active();
		if (np.isEmpty()) {
			np = new NowPlaying(grabbing ? LinkGrabber.message() : "Add a link in this mod's settings, or drop files in glide/music", null, 0, -1, false, 0);
		}
		float pad = 6;
		float textX = pad;
		this.drawBackground(W, H);
		if (icon.enabled()) {
			this.drawText(np.title == null || np.durationMs < 0 ? LegacyIcon.MUSIC : (np.playing ? LegacyIcon.PAUSE : LegacyIcon.PLAY), pad, 10.5F, 13, Fonts.LEGACYICON);
			textX += 18;
		}
		float maxW = W - textX - pad;
		this.drawText(fit(np.title, 9, Fonts.SEMIBOLD, maxW), textX, 7, 9, Fonts.SEMIBOLD);
		String sub = np.artist != null ? np.artist : (np.playing ? "" : (np.title != null && np.durationMs >= 0 ? "Paused" : ""));
		if (!np.playing && np.artist != null && np.durationMs >= 0) {
			sub = "Paused · " + np.artist;
		}
		this.drawText(fit(sub, 7.5F, Fonts.REGULAR, maxW), textX, 18, 7.5F, Fonts.REGULAR);
		if (progress.enabled() && np.durationMs > 0) {
			float frac = Math.min(1F, np.positionNow() / (float) np.durationMs);
			this.drawRoundedRect(textX, H - 8, maxW, 2.5F, 1.25F, this.getFontColor(60));
			this.drawRoundedRect(textX, H - 8, maxW * frac, 2.5F, 1.25F, this.getFontColor());
		} else if (grabbing) {
			float frac = LinkGrabber.progress();
			if (frac >= 0F) {
				this.drawRoundedRect(textX, H - 8, maxW, 2.5F, 1.25F, this.getFontColor(60));
				this.drawRoundedRect(textX, H - 8, maxW * Math.min(1F, frac), 2.5F, 1.25F, this.getFontColor());
			}
		}
		this.setWidth((int) W);
		this.setHeight((int) H);
	}

	private String fit(String s, float size, Font font, float maxW) {
		if (s == null || s.isEmpty()) {
			return "";
		}
		if (this.getTextWidth(s, size, font) <= maxW) {
			return s;
		}
		int lo = 0, hi = s.length();
		while (lo < hi) {
			int mid = (lo + hi + 1) / 2;
			if (this.getTextWidth(s.substring(0, mid) + "…", size, font) <= maxW) {
				lo = mid;
			} else {
				hi = mid - 1;
			}
		}
		return s.substring(0, lo) + "…";
	}
}
