package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 ParticleCustomizerMod — registered for menu parity; deepen hooks in follow-ups. */
public final class ParticleCustomizerModule extends Module {
	public ParticleCustomizerModule() {
		super("particle_customizer", Category.RENDER, "tt:PARTICLE_CUSTOMIZER", "tt:PARTICLE_CUSTOMIZER_DESCRIPTION");
	}
}
