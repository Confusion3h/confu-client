package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;

/** 1.8.9 HypixelMod: AutoGG and helpers. */
public final class HypixelModule extends Module {

	private final BooleanSetting autoGg = bool("autogg", "tt:AUTO_GG", true);

	public HypixelModule() {
		super("hypixel", Category.OTHER, "tt:HYPIXEL", "tt:HYPIXEL_DESCRIPTION");
	}

	public boolean autoGg() { return isEnabled() && autoGg.enabled(); }
}
