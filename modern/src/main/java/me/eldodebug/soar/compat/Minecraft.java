package me.eldodebug.soar.compat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/** 1.8.9 Minecraft facade over 1.20.1 MinecraftClient so the ported GUI code reads the same. */
public final class Minecraft {

	private static final Minecraft INSTANCE = new Minecraft();

	public static Minecraft getMinecraft() {
		return INSTANCE;
	}

	public MinecraftClient raw() {
		return MinecraftClient.getInstance();
	}

	public int displayWidth() {
		return raw().getWindow().getFramebufferWidth();
	}

	public int displayHeight() {
		return raw().getWindow().getFramebufferHeight();
	}

	public void displayGuiScreen(Screen screen) {
		raw().setScreen(screen);
	}

	public Screen currentScreen() {
		return raw().currentScreen;
	}

	public void shutdown() {
		raw().scheduleStop();
	}
}
