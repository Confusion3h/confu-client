package me.eldodebug.confu;

import me.eldodebug.confu.config.ConfuConfig;
import me.eldodebug.confu.gui.MenuScreen;
import me.eldodebug.confu.lang.Lang;
import me.eldodebug.confu.module.ModuleManager;
import me.eldodebug.confu.module.impl.*;
import me.eldodebug.confu.module.impl.FreelookModule;
import me.eldodebug.confu.module.impl.KeystrokesModule;
import me.eldodebug.confu.module.impl.ZoomModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfuClient implements ClientModInitializer {

	public static final String MOD_ID = "confu";
	public static final Logger LOGGER = LoggerFactory.getLogger("Confu");

	private static ConfuClient instance;

	private ConfuConfig config;
	private ModuleManager modules;
	private KeyBinding menuKey;

	public static ConfuClient get() {
		return instance;
	}

	public ModuleManager modules() {
		return modules;
	}

	public ConfuConfig config() {
		return config;
	}

	public KeyBinding menuKey() {
		return menuKey;
	}

	public String version() {
		return FabricLoader.getInstance().getModContainer(MOD_ID)
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse("dev");
	}

	@Override
	public void onInitializeClient() {
		instance = this;

		config = ConfuConfig.load(FabricLoader.getInstance().getConfigDir().resolve("confu-modern.json"));
		Lang.setLanguage(config.language(Lang.detectFromGame()));

		modules = new ModuleManager();
		modules.register(new ZoomModule());
		modules.register(new FreelookModule());
		modules.register(new CrosshairModule());
		modules.register(new KeystrokesModule());
		modules.register(new CpsModule());
		modules.register(new FpsModule());
		modules.register(new PingModule());
		modules.register(new CoordsModule());
		modules.register(new ClockModule());
		modules.register(new ArmorStatusModule());
		modules.register(new MusicPlayerModule());
		modules.register(new CapeModule());
		modules.load(config);

		menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.confu.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.categories.confu"));

		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
		HudRenderCallback.EVENT.register((context, tickDelta) -> modules.renderHud(context, tickDelta));
	}

	private void onClientTick(MinecraftClient client) {
		while (menuKey.wasPressed()) {
			if (client.currentScreen == null) {
				client.setScreen(new MenuScreen());
			}
		}
		modules.tick();
	}

	public void saveConfig() {
		modules.save(config);
		config.save();
	}
}
