package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.TextHudModule;

public final class CoordsModule extends TextHudModule {
	public CoordsModule() {
		super("coords", "module.coords.name", "module.coords.desc", 0F, 0.26F);
	}

	@Override
	protected String text() {
		if (mc.player == null) {
			return "X 0 Y 0 Z 0";
		}
		return String.format("X %d Y %d Z %d", mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ());
	}
}
