package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 CustomHeldItemsMod — registered for menu parity; deepen hooks in follow-ups. */
public final class CustomHeldItemsModule extends Module {
	public CustomHeldItemsModule() {
		super("custom_held_items", Category.RENDER, "tt:CUSTOM_HELD_ITEMS", "tt:CUSTOM_HELD_ITEMS_DESCRIPTION");
	}
}
