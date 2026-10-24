package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventKey;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.event.impl.EventTick;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.KeybindSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.mods.settings.impl.TextSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import me.eldodebug.soar.management.music.MusicManager;
import me.eldodebug.soar.management.music.NowPlaying;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

/**
 * Music player HUD. Off by default; while off it is not registered on the event bus
 * and the music manager owns no threads. While on, rendering only reads a snapshot.
 */
public class MusicPlayerMod extends HUDMod {

	private static final float W = 150, H = 38;

	private final ComboSetting sourceSetting = new ComboSetting(TranslateText.MUSIC_SOURCE, this, TranslateText.MUSIC_LOCAL,
			new ArrayList<Option>(Arrays.asList(new Option(TranslateText.MUSIC_LOCAL), new Option(TranslateText.MUSIC_SPOTIFY))));
	private final TextSetting clientIdSetting = new TextSetting(TranslateText.SPOTIFY_CLIENT_ID, this, "");
	private final NumberSetting volumeSetting = new NumberSetting(TranslateText.VOLUME, this, 60, 0, 100, true);
	private final BooleanSetting shuffleSetting = new BooleanSetting(TranslateText.SHUFFLE, this, false);
	private final BooleanSetting progressSetting = new BooleanSetting(TranslateText.PROGRESS_BAR, this, true);
	private final BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);
	private final KeybindSetting playPauseKey = new KeybindSetting(TranslateText.PLAY_PAUSE_KEY, this, Keyboard.KEY_NONE);
	private final KeybindSetting nextKey = new KeybindSetting(TranslateText.NEXT_TRACK_KEY, this, Keyboard.KEY_NONE);
	private final KeybindSetting prevKey = new KeybindSetting(TranslateText.PREVIOUS_TRACK_KEY, this, Keyboard.KEY_NONE);

	private MusicManager.Source lastSource;
	private int lastVolume = -1;
	private boolean lastShuffle;

	private static MusicPlayerMod instance;

	public MusicPlayerMod() {
		super(TranslateText.MUSIC_PLAYER, TranslateText.MUSIC_PLAYER_DESCRIPTION, "music spotify");
		instance = this;
	}

	public static MusicPlayerMod getInstance() {
		return instance;
	}

	private MusicManager music() {
		return Glide.getInstance().getMusicManager();
	}

	private MusicManager.Source selectedSource() {
		return sourceSetting.getOption().getTranslate() == TranslateText.MUSIC_SPOTIFY ? MusicManager.Source.SPOTIFY : MusicManager.Source.LOCAL;
	}

	public String getClientId() {
		return clientIdSetting.getText();
	}

	@Override
	public void onEnable() {
		super.onEnable();
		lastSource = selectedSource();
		music().getSpotify().setClientId(getClientId());
		music().activate(lastSource);
		syncLocalSettings(true);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		music().deactivate();
	}

	private void syncLocalSettings(boolean force) {
		int vol = volumeSetting.getValueInt();
		if (force || vol != lastVolume) {
			lastVolume = vol;
			music().setLocalVolume(vol / 100f);
		}
		boolean sh = shuffleSetting.isToggled();
		if (force || sh != lastShuffle) {
			lastShuffle = sh;
			music().setShuffle(sh);
		}
	}

	/** 20 Hz, a handful of field compares: reacts to settings edited in the mod menu. */
	@EventTarget
	public void onTick(EventTick event) {
		MusicManager.Source s = selectedSource();
		if (s != lastSource) {
			lastSource = s;
			music().setSource(s);
		}
		syncLocalSettings(false);
	}

	@EventTarget
	public void onKey(EventKey event) {
		int k = event.getKeyCode();
		if (k == Keyboard.KEY_NONE || mc.currentScreen != null) {
			return;
		}
		if (k == playPauseKey.getKeyCode()) {
			music().togglePause();
		} else if (k == nextKey.getKeyCode()) {
			music().next();
		} else if (k == prevKey.getKeyCode()) {
			music().previous();
		}
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		NowPlaying np = music().getNowPlaying();
		if (np.isEmpty() && !isEditing()) {
			return; // nothing playing: no NanoVG frame at all
		}
		final NowPlaying shown = np.isEmpty() ? new NowPlaying(placeholder(), null, 0, -1, false, 0) : np;
		Glide.getInstance().getNanoVGManager().setupAndDraw(() -> drawNanoVG(shown));
	}

	private String placeholder() {
		if (selectedSource() == MusicManager.Source.SPOTIFY) {
			return music().getSpotify().isAuthorized() ? "Spotify: nothing playing" : "Spotify: .soarcmd music connect";
		}
		return "Drop music in glide/music";
	}

	private void drawNanoVG(NowPlaying np) {
		float pad = 6;
		float textX = pad;
		this.drawBackground(W, H);
		if (iconSetting.isToggled()) {
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
		if (progressSetting.isToggled() && np.durationMs > 0) {
			float frac = Math.min(1F, np.positionNow() / (float) np.durationMs);
			this.drawRoundedRect(textX, H - 8, maxW, 2.5F, 1.25F, this.getFontColor(60));
			this.drawRoundedRect(textX, H - 8, maxW * frac, 2.5F, 1.25F, this.getFontColor());
		}
		this.setWidth((int) W);
		this.setHeight((int) H);
	}

	private String fit(String s, float size, me.eldodebug.soar.management.nanovg.font.Font font, float maxW) {
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
