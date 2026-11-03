package me.eldodebug.confu.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** ARGB color setting (1.8.9 ColorSetting). */
public final class ColorSetting extends Setting<Integer> {

	private final boolean showAlpha;

	public ColorSetting(String key, String nameKey, int defaultArgb, boolean showAlpha) {
		super(key, nameKey, defaultArgb);
		this.showAlpha = showAlpha;
	}

	public boolean showAlpha() {
		return showAlpha;
	}

	public int argb() {
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
			value = element.getAsInt();
		}
	}
}
