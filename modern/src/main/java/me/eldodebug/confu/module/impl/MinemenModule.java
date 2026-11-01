package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;

/** 1.8.9 MinemenMod: run /requeue when Minemen's chat prompt appears. */
public final class MinemenModule extends Module {

	private final BooleanSetting autoPlay = bool("auto_play", "tt:AUTO_PLAY", false);

	public MinemenModule() {
		super("minemen", Category.OTHER, "tt:MINEMEN", "tt:MINEMEN_DESCRIPTION");
	}

	public boolean autoPlay() {
		return isEnabled() && autoPlay.enabled();
	}
}
