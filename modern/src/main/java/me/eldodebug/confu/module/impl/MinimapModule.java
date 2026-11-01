package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 MinimapMod — registered for menu parity; deepen hooks in follow-ups. */
public final class MinimapModule extends Module {
	public MinimapModule() {
		super("minimap", Category.HUD, "tt:MINIMAP", "tt:MINIMAP_DESCRIPTION");
	}
}
