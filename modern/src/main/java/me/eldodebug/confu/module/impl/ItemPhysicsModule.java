package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 ItemPhysicsMod: physical dropped-item orientation. */
public final class ItemPhysicsModule extends Module {
	public ItemPhysicsModule() {
		super("item_physics", Category.RENDER, "tt:ITEM_PHYSICS", "tt:ITEM_PHYSICS_DESCRIPTION");
	}
}
