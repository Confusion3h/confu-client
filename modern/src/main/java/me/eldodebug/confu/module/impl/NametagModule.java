package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 NametagMod: nametag scale / background toggles. */
public final class NametagModule extends Module {

	private final BooleanSetting background = bool("background", "tt:BACKGROUND", true);
	private final NumberSetting scale = number("scale", "tt:SCALE", 1, 0.5, 2, 0.05);

	public NametagModule() {
		super("nametag", Category.PLAYER, "tt:NAMETAG", "tt:NAMETAG_DESCRIPTION");
	}

	public boolean background() { return !isEnabled() || background.enabled(); }
	public float scale() { return isEnabled() ? scale.getFloat() : 1F; }
}
