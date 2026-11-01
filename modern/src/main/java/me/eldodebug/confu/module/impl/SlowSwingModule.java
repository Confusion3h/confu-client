package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 SlowSwingMod: custom swing animation delay. */
public final class SlowSwingModule extends Module {

	private final NumberSetting delay = number("delay", "tt:DELAY", 14, 2, 20, 1);

	public SlowSwingModule() {
		super("slow_swing", Category.PLAYER, "tt:SLOW_SWING", "tt:SLOW_SWING_DESCRIPTION");
	}

	public int delayTicks() {
		return isEnabled() ? delay.getInt() : -1;
	}
}
