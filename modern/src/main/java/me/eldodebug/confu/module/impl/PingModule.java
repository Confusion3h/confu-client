package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import net.minecraft.client.network.PlayerListEntry;

/** 1.8.9 PingDisplayMod. */
public final class PingModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);

	public PingModule() {
		super("ping", "tt:PING_DISPLAY", "tt:PING_DISPLAY_DESCRIPTION", 0F, 0.18F);
	}

	@Override
	public String getText() {
		int ping = 0;
		if (mc.player != null && mc.getNetworkHandler() != null) {
			PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
			ping = entry == null ? 0 : entry.getLatency();
		}
		return ping + " ms";
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.BAR_CHERT : null;
	}
}
