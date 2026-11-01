package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 ProjectileTrailMod — registered for menu parity; deepen hooks in follow-ups. */
public final class ProjectileTrailModule extends Module {
	public ProjectileTrailModule() {
		super("projectile_trail", Category.PLAYER, "tt:PROJECTILE_TRAIL", "tt:PROJECTILE_TRAIL_DESCRIPTION");
	}
}
