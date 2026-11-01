package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 BossHealthMod: scale boss bar. */
public final class BossHealthModule extends Module {

	private final NumberSetting scale = number("scale", "tt:SCALE", 1, 0.5, 1.5, 0.05);

	public BossHealthModule() {
		super("boss_health", Category.HUD, "tt:BOSS_HEALTH", "tt:BOSS_HEALTH_DESCRIPTION");
	}

	public float scale() {
		return isEnabled() ? scale.getFloat() : 1F;
	}
}
