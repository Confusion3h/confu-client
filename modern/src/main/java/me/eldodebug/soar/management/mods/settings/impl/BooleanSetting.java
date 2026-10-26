package me.eldodebug.soar.management.mods.settings.impl;

import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.settings.Setting;

public class BooleanSetting extends Setting {

	private final me.eldodebug.confu.module.setting.BooleanSetting raw;

	public BooleanSetting(Mod parent, me.eldodebug.confu.module.setting.BooleanSetting raw) {
		super(parent, raw);
		this.raw = raw;
	}

	public boolean isToggled() {
		return raw.enabled();
	}

	public void setToggled(boolean toggle) {
		raw.set(toggle);
		changed();
	}
}
