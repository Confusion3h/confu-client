package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;

/** 1.8.9 FPSBoostMod: optional entity/particle culling toggles (Sodium covers most). */
public final class FpsBoostModule extends Module {

	private final BooleanSetting lowParticles = bool("low_particles", "tt:PARTICLE", false);

	public FpsBoostModule() {
		super("fps_boost", Category.OTHER, "tt:FPS_BOOST", "tt:FPS_BOOST_DESCRIPTION");
	}

	public boolean lowParticles() { return isEnabled() && lowParticles.enabled(); }
}
