package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 InventoryMod — registered for menu parity; deepen hooks in follow-ups. */
public final class InventoryModule extends Module {
	public InventoryModule() {
		super("inventory", Category.OTHER, "tt:INVENTORY", "tt:INVENTORY_DESCRIPTION");
	}
}
