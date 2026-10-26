package me.eldodebug.soar.management.mods.settings.impl;

import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.settings.Setting;

public class KeybindSetting extends Setting {

	private final me.eldodebug.confu.module.setting.KeybindSetting raw;

	public KeybindSetting(Mod parent, me.eldodebug.confu.module.setting.KeybindSetting raw) {
		super(parent, raw);
		this.raw = raw;
	}

	public int getKeyCode() {
		return raw.code();
	}

	public void setKeyCode(int keyCode) {
		raw.set(keyCode);
		changed();
	}
}
