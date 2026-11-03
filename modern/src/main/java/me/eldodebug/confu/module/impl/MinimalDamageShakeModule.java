package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 MinimalDamageShakeMod / nohurtcam. */
public final class MinimalDamageShakeModule extends Module {

	private final NumberSetting intensity = number("intensity", "tt:INTENSITY", 0, 0, 100, 1);

	public MinimalDamageShakeModule() {
		super("minimal_damage_shake", Category.RENDER, "tt:MINIMAL_DAMAGE_SHAKE", "tt:MINIMAL_DAMAGE_SHAKE_DESCRIPTION");
	}

	@Override
	public String alias() {
		return "nohurtcam hurtcam hurt cam";
	}

	/** Multiplier for hurt camera tilt (0 = none). */
	public float intensityFactor() {
		return isEnabled() ? intensity.getFloat() / 100F : 1F;
	}
}
