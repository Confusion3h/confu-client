package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 BloodParticlesMod — registered for menu parity; deepen hooks in follow-ups. */
public final class BloodParticlesModule extends Module {
	public BloodParticlesModule() {
		super("blood_particles", Category.RENDER, "tt:BLOOD_PARTICLES", "tt:BLOOD_PARTICLES_DESCRIPTION");
	}
}
