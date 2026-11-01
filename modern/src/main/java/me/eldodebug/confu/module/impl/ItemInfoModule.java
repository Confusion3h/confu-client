package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 ItemInfoMod — registered for menu parity; deepen hooks in follow-ups. */
public final class ItemInfoModule extends Module {
	public ItemInfoModule() {
		super("item_info", Category.RENDER, "tt:ITEM_INFO", "tt:ITEM_INFO_DESCRIPTION");
	}
}
