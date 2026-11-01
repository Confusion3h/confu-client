package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 PlayerDisplayMod — registered for menu parity; deepen hooks in follow-ups. */
public final class PlayerDisplayModule extends Module {
	public PlayerDisplayModule() {
		super("player_display", Category.HUD, "tt:PLAYER_DISPLAY", "tt:PLAYER_DISPLAY_DESCRIPTION");
	}
}
