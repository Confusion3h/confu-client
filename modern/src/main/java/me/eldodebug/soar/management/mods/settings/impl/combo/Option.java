package me.eldodebug.soar.management.mods.settings.impl.combo;

import me.eldodebug.confu.lang.Lang;

public class Option {

	private final String value;

	public Option(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public String getName() {
		String key = "option." + value;
		String text = Lang.get(key);
		if (!text.equals(key)) {
			return text;
		}
		return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}

	public String getNameKey() {
		return value;
	}
}
