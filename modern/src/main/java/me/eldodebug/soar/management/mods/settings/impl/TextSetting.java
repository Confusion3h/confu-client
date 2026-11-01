package me.eldodebug.soar.management.mods.settings.impl;

import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.settings.Setting;

/** 1.8.9 TextSetting over a 1.20.1 StringSetting. */
public class TextSetting extends Setting {

	private final me.eldodebug.confu.module.setting.StringSetting raw;

	public TextSetting(Mod parent, me.eldodebug.confu.module.setting.StringSetting raw) {
		super(parent, raw);
		this.raw = raw;
	}

	public String getText() {
		return raw.get();
	}

	public void setText(String text) {
		raw.set(text);
	}

	public void commit() {
		changed();
	}
}
