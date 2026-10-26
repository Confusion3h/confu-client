package me.eldodebug.soar.compat;

import net.minecraft.client.MinecraftClient;

/** LWJGL2 Mouse facade: wheel deltas are fed by GuiScreen.mouseScrolled (120 per notch, like LWJGL2). */
public final class Mouse {

	private static int wheel;

	private Mouse() {
	}

	public static void addWheel(double amount) {
		wheel += (int) Math.round(amount * 120);
	}

	public static int getDWheel() {
		int w = wheel;
		wheel = 0;
		return w;
	}

	public static int getX() {
		return (int) MinecraftClient.getInstance().mouse.getX();
	}

	public static int getY() {
		MinecraftClient mc = MinecraftClient.getInstance();
		return mc.getWindow().getHeight() - (int) mc.mouse.getY();
	}
}
