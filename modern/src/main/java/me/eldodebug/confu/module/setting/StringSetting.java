package me.eldodebug.confu.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class StringSetting extends Setting<String> {

	public StringSetting(String key, String nameKey, String defaultValue) {
		super(key, nameKey, defaultValue == null ? "" : defaultValue);
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
