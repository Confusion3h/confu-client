package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

/** 1.8.9 PlayerCounterMod. */
public final class PlayerCounterModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);

	public PlayerCounterModule() {
		super("player_counter", "tt:PLAYER_COUNTER", "tt:PLAYER_COUNTER_DESCRIPTION", 0.15F, 0.58F);
	}

	@Override
	public String getText() {
		int n = 1;
		if (mc.getNetworkHandler() != null) {
			n = mc.getNetworkHandler().getPlayerList().size();
		}
		return "Player: " + n;
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.USERS : null;
	}
}
