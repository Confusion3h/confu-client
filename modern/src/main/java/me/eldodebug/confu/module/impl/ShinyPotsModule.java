package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 ShinyPotsMod — registered for menu parity; deepen hooks in follow-ups. */
public final class ShinyPotsModule extends Module {
	public ShinyPotsModule() {
		super("shiny_pots", Category.RENDER, "tt:SHINY_POTS", "tt:SHINY_POTS_DESCRIPTION");
	}
}
