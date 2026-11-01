package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 FPSSpooferMod: multiplies displayed FPS. */
public final class FpsSpooferModule extends Module {

	private final NumberSetting multiplier = number("multiplier", "tt:MULTIPLIER", 2, 1, 30, 1);

	public FpsSpooferModule() {
		super("fps_spoofer", Category.OTHER, "tt:FPS_SPOOFER", "tt:FPS_SPOOFER_DESCRIPTION");
	}

	public int spoof(int real) {
		return isEnabled() ? real * multiplier.getInt() : real;
	}
}
