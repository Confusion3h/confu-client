package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 AnimationsMod (old animations toggles). */
public final class AnimationsModule extends Module {

	private final BooleanSetting blockHit = bool("block_hit", "tt:BLOCK_HIT", true);
	private final BooleanSetting pushing = bool("pushing", "tt:PUSHING", true);
	private final BooleanSetting sneak = bool("sneak", "tt:SNEAK", true);
	private final BooleanSetting smoothSneak = bool("smooth_sneak", "tt:SNEAKSMOOTH", false);
	private final NumberSetting smoothSpeed = number("smooth_speed", "tt:SMOOTH_SPEED", 6, 0.5, 20, 0.5);

	public AnimationsModule() {
		super("animations", Category.RENDER, "tt:OLD_ANIMATION", "tt:OLD_ANIMATION_DESCRIPTION");
	}

	public boolean blockHit() {
		return isEnabled() && blockHit.enabled();
	}

	public boolean smoothSneak() {
		return isEnabled() && smoothSneak.enabled();
	}

	public float smoothSpeed() {
		return smoothSpeed.getFloat();
	}
}
