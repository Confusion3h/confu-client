package me.eldodebug.soar.management.mods.settings.impl;

import java.util.ArrayList;

import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.settings.Setting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;

public class ComboSetting extends Setting {

	private final me.eldodebug.confu.module.setting.ComboSetting raw;
	private final ArrayList<Option> options = new ArrayList<>();

	public ComboSetting(Mod parent, me.eldodebug.confu.module.setting.ComboSetting raw) {
		super(parent, raw);
		this.raw = raw;
		for (String o : raw.options()) {
			options.add(new Option(o));
		}
	}

	public Option getOption() {
		for (Option o : options) {
			if (o.getValue().equals(raw.get())) {
				return o;
			}
		}
		return options.get(0);
	}

	public void setOption(Option option) {
		raw.set(option.getValue());
		changed();
	}

	public ArrayList<Option> getOptions() {
		return options;
	}
}
