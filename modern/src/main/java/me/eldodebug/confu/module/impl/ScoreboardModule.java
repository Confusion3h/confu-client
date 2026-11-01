package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 ScoreboardMod: hide/background/scale scoreboard. */
public final class ScoreboardModule extends Module {

	private final BooleanSetting background = bool("background", "tt:BACKGROUND", true);
	private final BooleanSetting numbers = bool("numbers", "tt:NUMBER", true);
	private final NumberSetting scale = number("scale", "tt:SCALE", 1, 0.5, 1.5, 0.05);

	public ScoreboardModule() {
		super("scoreboard", Category.HUD, "tt:SCOREBOARD", "tt:SCOREBOARD_DESCRIPTION");
	}

	public boolean showBackground() { return !isEnabled() || background.enabled(); }
	public boolean showNumbers() { return !isEnabled() || numbers.enabled(); }
	public float scale() { return isEnabled() ? scale.getFloat() : 1F; }
}
