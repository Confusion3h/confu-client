package me.eldodebug.soar.compat;

import net.minecraft.client.util.Window;

public final class ScaledResolution {

	private final int width, height, factor;

	public ScaledResolution(Minecraft mc) {
		Window w = mc.raw().getWindow();
		this.width = w.getScaledWidth();
		this.height = w.getScaledHeight();
		this.factor = (int) Math.round(w.getScaleFactor());
	}

	public int getScaledWidth() {
		return width;
	}

	public int getScaledHeight() {
		return height;
	}

	public int getScaleFactor() {
		return factor;
	}
}
