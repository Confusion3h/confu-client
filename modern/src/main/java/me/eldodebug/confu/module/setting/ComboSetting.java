package me.eldodebug.confu.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

public final class ComboSetting extends Setting<String> {

	private final List<String> options;

	public ComboSetting(String key, String nameKey, String defaultValue, List<String> options) {
		super(key, nameKey, options.contains(defaultValue) ? defaultValue : options.get(0));
		this.options = List.copyOf(options);
	}

	public List<String> options() {
		return options;
	}

	public void cycle() {
		int index = options.indexOf(value);
		value = options.get((index + 1) % options.size());
	}

	@Override
	public void set(String value) {
		if (options.contains(value)) {
			super.set(value);
		}
	}

	@Override
	public void write(JsonObject json) {
		json.addProperty(key(), value);
	}

	@Override
	public void read(JsonObject json) {
		JsonElement element = json.get(key());
		if (element != null && element.isJsonPrimitive()) {
			set(element.getAsString());
		}
	}
}
