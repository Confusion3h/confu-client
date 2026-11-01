package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 EntityCullingMod — registered for menu parity; deepen hooks in follow-ups. */
public final class EntityCullingModule extends Module {
	public EntityCullingModule() {
		super("entity_culling", Category.OTHER, "tt:ENTITY_CULLING", "tt:ENTITY_CULLING_DESCRIPTION");
	}
}
