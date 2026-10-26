package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 FullbrightMod: gamma 20 while on (LightmapTextureManagerMixin reads {@link #gamma}). */
public final class FullbrightModule extends Module {

	private static boolean active;

	public FullbrightModule() {
		super("fullbright", Category.RENDER, "tt:FULLBRIGHT", "tt:FULLBRIGHT_DESCRIPTION");
	}

	public static boolean active() {
		return active;
	}

	@Override
	protected void onEnable() {
		active = true;
	}

	@Override
	protected void onDisable() {
		active = false;
	}
}
