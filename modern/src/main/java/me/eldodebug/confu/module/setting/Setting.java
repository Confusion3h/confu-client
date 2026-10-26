package me.eldodebug.confu.module.setting;

import com.google.gson.JsonObject;

public abstract class Setting<T> {

	private final String key;
	private final String nameKey;
	private final T defaultValue;
	protected T value;

	protected Setting(String key, String nameKey, T defaultValue) {
		this.key = key;
		this.nameKey = nameKey;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public String key() {
		return key;
	}

	public String nameKey() {
		return nameKey;
	}

	public T get() {
		return value;
	}

	public void set(T value) {
		this.value = value;
	}

	public void reset() {
		this.value = defaultValue;
	}

	public abstract void write(JsonObject json);

	public abstract void read(JsonObject json);
}
