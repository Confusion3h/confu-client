package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.TextHudModule;
import net.minecraft.client.network.PlayerListEntry;

public final class PingModule extends TextHudModule {
	public PingModule() {
		super("ping", "module.ping.name", "module.ping.desc", 0F, 0.18F);
	}

	@Override
	protected String text() {
		if (mc.player == null || mc.getNetworkHandler() == null) {
			return "0 ms";
		}
		PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
		return (entry == null ? 0 : entry.getLatency()) + " ms";
	}
}
