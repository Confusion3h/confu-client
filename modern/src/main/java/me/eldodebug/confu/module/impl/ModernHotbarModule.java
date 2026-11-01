package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 ModernHotbarMod — registered for menu parity; deepen hooks in follow-ups. */
public final class ModernHotbarModule extends Module {
	public ModernHotbarModule() {
		super("modern_hotbar", Category.HUD, "tt:MODERN_HOTBAR", "tt:MODERN_HOTBAR_DESCRIPTION");
	}
}
