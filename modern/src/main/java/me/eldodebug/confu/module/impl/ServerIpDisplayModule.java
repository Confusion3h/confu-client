package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

/** 1.8.9 ServerIPDisplayMod. */
public final class ServerIpDisplayModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);

	public ServerIpDisplayModule() {
		super("server_ip_display", "tt:SERVER_IP", "tt:SERVER_IP_DISPLAY_DESCRIPTION", 0.30F, 0.02F);
	}

	@Override
	public String getText() {
		if (mc.isInSingleplayer() || mc.getCurrentServerEntry() == null) {
			return "Single Player";
		}
		return mc.getCurrentServerEntry().address;
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.SERVER : null;
	}
}
