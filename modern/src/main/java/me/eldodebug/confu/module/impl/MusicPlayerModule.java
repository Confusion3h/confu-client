package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.HudModule;
import me.eldodebug.confu.lang.Lang;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.KeybindSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.music.MusicPlayer;
import me.eldodebug.confu.render.Draw;
import me.eldodebug.confu.render.Theme;
import me.eldodebug.confu.util.Keys;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public final class MusicPlayerModule extends HudModule {

	private static final int W = 170;
	private static final int H = 34;

	private final NumberSetting volume = number("volume", "setting.music.volume", 0.5, 0, 1, 0.05);
	private final BooleanSetting shuffle = bool("shuffle", "setting.music.shuffle", false);
	private final KeybindSetting nextKey = extraKey("next_key", "setting.music.next", GLFW.GLFW_KEY_RIGHT_BRACKET);
	private final KeybindSetting pauseKey = extraKey("pause_key", "setting.music.pause", GLFW.GLFW_KEY_LEFT_BRACKET);

	private final MusicPlayer player = new MusicPlayer(FabricLoader.getInstance().getGameDir().resolve("confu").resolve("music"));
	private boolean nextWasDown;
	private boolean pauseWasDown;

	public MusicPlayerModule() {
		super("music_player", Category.HUD, "module.music.name", "module.music.desc", 0.5F, 0F);
	}

	@Override
	protected void onEnable() {
		player.scan(shuffle.enabled());
		player.setVolume(volume.getFloat());
		player.play(0);
	}

	@Override
	protected void onDisable() {
		player.stopPlayback();
	}

	@Override
	public void onTick() {
		player.setVolume(volume.getFloat());
		boolean free = mc.currentScreen == null;
		boolean next = free && nextKey.bound() && Keys.isDown(nextKey.code());
		boolean pause = free && pauseKey.bound() && Keys.isDown(pauseKey.code());
		if (next && !nextWasDown) {
			player.next();
		}
		if (pause && !pauseWasDown) {
			player.togglePause();
		}
		nextWasDown = next;
		pauseWasDown = pause;
	}

	@Override
	protected int width() {
		return W;
	}

	@Override
	protected int height() {
		return H;
	}

	@Override
	protected void draw(DrawContext context, float tickDelta) {
		Draw.roundRect(context, 0, 0, W, H, 6, Theme.KEY);
		Draw.roundRect(context, 5, 5, 24, 24, 5, Theme.ACCENT);
		Draw.textCentered(context, mc.textRenderer, player.paused() ? "II" : "\u266A", 17, 13, Theme.ACCENT_TEXT);
		String title = player.current();
		String line1 = title == null ? Lang.get("music.empty") : title;
		String line2 = title == null ? "confu/music (.ogg, .wav)"
				: (player.paused() ? Lang.get("music.paused") : Lang.get("music.playing"))
				+ (player.status().isEmpty() ? "" : " - " + player.status());
		Draw.text(context, mc.textRenderer, mc.textRenderer.trimToWidth(line1, W - 40), 35, 7, Theme.TEXT);
		Draw.text(context, mc.textRenderer, mc.textRenderer.trimToWidth(line2, W - 40), 35, 19, Theme.MUTED);
	}
}
