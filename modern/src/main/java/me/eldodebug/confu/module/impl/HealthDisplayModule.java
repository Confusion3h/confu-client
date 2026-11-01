package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

/** 1.8.9 HealthDisplayMod. */
public final class HealthDisplayModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);

	public HealthDisplayModule() {
		super("health_display", "tt:HEALTH_DISPLAY", "tt:HEALTH_DISPLAY_DESCRIPTION", 0.15F, 0.10F);
	}

	@Override
	protected boolean visible() {
		return mc.player != null;
	}

	@Override
	public String getText() {
		return (mc.player == null ? 0 : (int) mc.player.getHealth()) + " Health";
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.HEART : null;
	}
}
