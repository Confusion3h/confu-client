package me.eldodebug.confu.module;

import com.google.gson.JsonObject;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.confu.module.setting.KeybindSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.module.setting.Setting;
import me.eldodebug.confu.lang.Lang;
import me.eldodebug.confu.util.Keys;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {

	protected final MinecraftClient mc = MinecraftClient.getInstance();

	private final String id;
	private final Category category;
	private final String nameKey;
	private final String descriptionKey;
	private final List<Setting<?>> settings = new ArrayList<>();
	private KeybindSetting keybind;
	private boolean enabled;
	private boolean keyDown;
	private boolean keyEdge;

	protected Module(String id, Category category, String nameKey, String descriptionKey) {
		this.id = id;
		this.category = category;
		this.nameKey = nameKey;
		this.descriptionKey = descriptionKey;
	}

	public String id() {
		return id;
	}

	public Category category() {
		return category;
	}

	public String name() {
		return Lang.get(nameKey);
	}

	public String description() {
		return Lang.get(descriptionKey);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}
		applyEnabled(enabled);
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	private void applyEnabled(boolean value) {
		this.enabled = value;
		if (value) {
			onEnable();
		} else {
			onDisable();
		}
	}

	protected void onEnable() {
	}

	protected void onDisable() {
	}

	public void onTick() {
	}

	public void renderHud(DrawContext context, float tickDelta) {
	}

	public List<Setting<?>> settings() {
		return Collections.unmodifiableList(settings);
	}

	public KeybindSetting keybind() {
		return keybind;
	}

	protected final BooleanSetting bool(String key, String nameKey, boolean defaultValue) {
		return add(new BooleanSetting(key, nameKey, defaultValue));
	}

	protected final NumberSetting number(String key, String nameKey, double defaultValue, double min, double max, double step) {
		return add(new NumberSetting(key, nameKey, defaultValue, min, max, step));
	}

	protected final ComboSetting combo(String key, String nameKey, String defaultValue, String... options) {
		return add(new ComboSetting(key, nameKey, defaultValue, List.of(options)));
	}

	protected final KeybindSetting keybind(int defaultCode) {
		keybind = add(new KeybindSetting("keybind", "setting.keybind", defaultCode));
		return keybind;
	}

	private <S extends Setting<?>> S add(S setting) {
		settings.add(setting);
		return setting;
	}

	/** Held down, and the menu and chat are closed, so hotkeys do not fire while typing. */
	protected final boolean keyDown() {
		return keyDown;
	}

	/** Rising edge of the keybind on this tick. */
	protected final boolean keyPressed() {
		return keyEdge;
	}

	void tickKeys() {
		boolean down = keybind != null && keybind.bound() && mc.currentScreen == null && Keys.isDown(keybind.code());
		keyEdge = down && !keyDown;
		keyDown = down;
	}

	public void read(JsonObject json) {
		boolean wasEnabled = json.has("enabled") && json.get("enabled").getAsBoolean();
		for (Setting<?> setting : settings) {
			setting.read(json);
		}
		if (wasEnabled != enabled) {
			applyEnabled(wasEnabled);
		}
	}

	public void write(JsonObject json) {
		json.addProperty("enabled", enabled);
		for (Setting<?> setting : settings) {
			setting.write(json);
		}
	}
}
