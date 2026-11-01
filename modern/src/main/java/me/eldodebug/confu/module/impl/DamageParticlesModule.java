package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 DamageParticlesMod — registered for menu parity; deepen hooks in follow-ups. */
public final class DamageParticlesModule extends Module {
	public DamageParticlesModule() {
		super("damage_particles", Category.RENDER, "tt:DAMAGE_PARTICLES", "tt:DAMAGE_PARTICLES_DESCRIPTION");
	}
}
