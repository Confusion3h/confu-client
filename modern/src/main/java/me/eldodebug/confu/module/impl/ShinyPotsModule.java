package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 ShinyPotsMod: drops the equal-depth glint mask so potions glint through. */
public final class ShinyPotsModule extends Module {

	private static ShinyPotsModule instance;

	public ShinyPotsModule() {
		super("shiny_pots", Category.RENDER, "tt:SHINY_POTS", "tt:SHINY_POTS_DESCRIPTION");
		instance = this;
	}

	public static boolean active() {
		return instance != null && instance.isEnabled();
	}
}
