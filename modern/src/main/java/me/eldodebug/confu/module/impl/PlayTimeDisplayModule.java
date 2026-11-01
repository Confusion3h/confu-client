package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

/** 1.8.9 PlayTimeDisplayMod. */
public final class PlayTimeDisplayModule extends SimpleNvgHudModule {

	private static final long LAUNCH = System.currentTimeMillis();
	private final BooleanSetting icon = bool("icon", "tt:ICON", true);

	public PlayTimeDisplayModule() {
		super("play_time_display", "tt:PLAY_TIME_DISPLAY", "tt:PLAY_TIME_DISPLAY_DESCRIPTION", 0.15F, 0.50F);
	}

	@Override
	public String getText() {
		int sec = (int) ((System.currentTimeMillis() - LAUNCH) / 1000);
		int min = (sec % 3600) / 60;
		int hour = sec / 3600;
		sec = sec % 60;
		return String.format("%02d:%02d:%02d", hour, min, sec);
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.CLOCK : null;
	}
}
