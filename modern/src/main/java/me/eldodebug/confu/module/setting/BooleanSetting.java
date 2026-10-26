package me.eldodebug.confu.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class BooleanSetting extends Setting<Boolean> {

	public BooleanSetting(String key, String nameKey, boolean defaultValue) {
		super(key, nameKey, defaultValue);
	}

	public boolean enabled() {
		return value;
	}

	public void toggle() {
		value = !value;
	}

	@Override
	public void write(JsonObject json) {
		json.addProperty(key(), value);
	}

	@Override
	public void read(JsonObject json) {
		JsonElement element = json.get(key());
		if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
			value = element.getAsBoolean();
		}
	}
}
