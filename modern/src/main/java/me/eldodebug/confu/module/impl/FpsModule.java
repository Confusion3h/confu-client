package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

/** 1.8.9 FPSDisplayMod. */
public final class FpsModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);

	public FpsModule() {
		super("fps", "tt:FPS_DISPLAY", "tt:FPS_DISPLAY_DESCRIPTION", 0F, 0.02F);
	}

	@Override
	public String getText() {
		return mc.getCurrentFps() + " FPS";
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.MONITOR : null;
	}
}
