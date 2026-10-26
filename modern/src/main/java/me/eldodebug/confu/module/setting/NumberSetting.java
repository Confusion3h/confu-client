package me.eldodebug.confu.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class NumberSetting extends Setting<Double> {

	private final double min;
	private final double max;
	private final double step;

	public NumberSetting(String key, String nameKey, double defaultValue, double min, double max, double step) {
		super(key, nameKey, clamp(defaultValue, min, max, step));
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public float getFloat() {
		return value.floatValue();
	}

	public int getInt() {
		return (int) Math.round(value);
	}

	public double min() {
		return min;
	}

	public double step() {
		return step;
	}

	public double max() {
		return max;
	}

	public double ratio() {
		return (value - min) / (max - min);
	}

	public void setRatio(double ratio) {
		double raw = min + Math.max(0D, Math.min(1D, ratio)) * (max - min);
		set(raw);
	}

	@Override
	public void set(Double value) {
		super.set(clamp(value, min, max, step));
	}

	@Override
	public void write(JsonObject json) {
		json.addProperty(key(), value);
	}

	@Override
	public void read(JsonObject json) {
		JsonElement element = json.get(key());
		if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
			set(element.getAsDouble());
		}
	}

	private static double clamp(double value, double min, double max, double step) {
		double clamped = Math.max(min, Math.min(max, value));
		if (step <= 0D) {
			return clamped;
		}
		double snapped = min + Math.round((clamped - min) / step) * step;
		return Math.max(min, Math.min(max, snapped));
	}
}
