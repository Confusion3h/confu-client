package me.eldodebug.soar.management.mods.settings.impl;

import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.settings.Setting;

public class NumberSetting extends Setting {

	private final me.eldodebug.confu.module.setting.NumberSetting raw;

	public NumberSetting(Mod parent, me.eldodebug.confu.module.setting.NumberSetting raw) {
		super(parent, raw);
		this.raw = raw;
	}

	public double getValue() {
		return raw.get();
	}

	public int getValueInt() {
		return raw.getInt();
	}

	public float getValueFloat() {
		return raw.getFloat();
	}

	public void setValue(double value) {
		raw.set(value);
	}

	/** Saved once the slider is released, not on every drag frame. */
	public void commit() {
		changed();
	}

	public double getMinValue() {
		return raw.min();
	}

	public double getMaxValue() {
		return raw.max();
	}
}
