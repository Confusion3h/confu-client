package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 PlayerListMod — registered for menu parity; deepen hooks in follow-ups. */
public final class PlayerListModule extends Module {
	public PlayerListModule() {
		super("player_list", Category.HUD, "tt:PLAYER_LIST", "tt:PLAYER_LIST_DESCRIPTION");
	}
}
