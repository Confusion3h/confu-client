package me.eldodebug.confu.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.eldodebug.confu.util.Keys;

public final class KeybindSetting extends Setting<Integer> {

	public KeybindSetting(String key, String nameKey, int defaultCode) {
		super(key, nameKey, defaultCode);
	}

	public boolean bound() {
		return value != Keys.NONE;
	}

	public int code() {
		return value;
	}

	@Override
	public void write(JsonObject json) {
		json.addProperty(key(), value);
	}

	@Override
	public void read(JsonObject json) {
		JsonElement element = json.get(key());
		if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
			set(element.getAsInt());
		}
	}
}
