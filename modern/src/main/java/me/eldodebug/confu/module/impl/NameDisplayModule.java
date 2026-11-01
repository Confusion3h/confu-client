package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

/** 1.8.9 NameDisplayMod. */
public final class NameDisplayModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);
	private final ComboSetting prefix = combo("prefix", "tt:PREFIX", "name", "name", "ign");

	public NameDisplayModule() {
		super("name_display", "tt:NAME_DISPLAY", "tt:NAME_DISPLAY_DESCRIPTION", 0.15F, 0.42F);
	}

	@Override
	protected boolean visible() {
		return mc.player != null;
	}

	@Override
	public String getText() {
		String p = prefix.get().equals("ign") ? "Ign" : "Name";
		String name = mc.player == null ? "?" : mc.player.getGameProfile().getName();
		return p + ": " + name;
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.USER : null;
	}
}
