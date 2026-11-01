package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 Items2DMod: flat ground-item rendering (mixin hook). */
public final class Items2DModule extends Module {
	public Items2DModule() {
		super("items_2d", Category.RENDER, "tt:ITEMS_2D", "tt:ITEMS_2D_DESCRIPTION");
	}
}
