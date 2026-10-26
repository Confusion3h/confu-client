package me.eldodebug.confu.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.eldodebug.confu.config.ConfuConfig;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {

	private final List<Module> modules = new ArrayList<>();

	public <T extends Module> T register(T module) {
		modules.add(module);
		return module;
	}

	public List<Module> all() {
		return Collections.unmodifiableList(modules);
	}

	public List<Module> inCategory(Category category) {
		List<Module> out = new ArrayList<>();
		for (Module module : modules) {
			if (category == Category.ALL || module.category() == category) {
				out.add(module);
			}
		}
		return out;
	}

	public <T extends Module> T get(Class<T> type) {
		for (Module module : modules) {
			if (type.isInstance(module)) {
				return type.cast(module);
			}
		}
		throw new IllegalStateException("Module not registered: " + type.getName());
	}

	public void tick() {
		for (Module module : modules) {
			module.tickKeys();
			if (module.isEnabled()) {
				module.onTick();
			}
		}
	}

	public void renderHud(DrawContext context, float tickDelta) {
		for (Module module : modules) {
			if (module.isEnabled()) {
				module.renderHud(context, tickDelta);
			}
		}
	}

	public void load(ConfuConfig config) {
		JsonObject section = config.modules();
		for (Module module : modules) {
			JsonElement element = section.get(module.id());
			if (element != null && element.isJsonObject()) {
				module.read(element.getAsJsonObject());
			}
		}
	}

	public void save(ConfuConfig config) {
		JsonObject section = config.modules();
		for (Module module : modules) {
			JsonObject json = new JsonObject();
			module.write(json);
			section.add(module.id(), json);
		}
	}
}
