package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 KillEffectsMod — registered for menu parity; deepen hooks in follow-ups. */
public final class KillEffectsModule extends Module {
	public KillEffectsModule() {
		super("kill_effects", Category.RENDER, "tt:KILL_EFFECTS", "tt:KILL_EFFECTS_DESCRIPTION");
	}
}
