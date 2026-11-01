package me.eldodebug.confu.module.impl;

import java.text.DecimalFormat;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import net.minecraft.util.math.MathHelper;

/** 1.8.9 SpeedometerMod (simple text mode; graph later parity polish). */
public final class SpeedometerModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);
	private final BooleanSetting graph = bool("graph", "tt:GRAPH", false);
	private final DecimalFormat speedFormat = new DecimalFormat("0.00");
	private final int speedCount = 200;
	private final double[] speeds = new double[speedCount];
	private long lastUpdate = -1;

	public SpeedometerModule() {
		super("speedometer", "tt:SPEEDOMETER", "tt:SPEEDOMETER_DESCRIPTION", 0.30F, 0.18F);
	}

	@Override
	protected boolean visible() {
		return mc.player != null;
	}

	@Override
	protected void drawNanoVG() {
		if (!graph.enabled() || mc.player == null) {
			drawSimple();
			return;
		}
		if (!mc.isPaused() && (lastUpdate < 0 || System.currentTimeMillis() - lastUpdate > 30)) {
			addSpeed(getSpeed() / 5.0);
			lastUpdate = System.currentTimeMillis();
		}
		this.drawBackground(155, 100);
		this.drawRect(0, 17.5F, 155, 1, getFontColor(120));
		this.drawText("Speed: " + speedFormat.format(getSpeed()) + " m/s", 5.5F, 6F, 10.5F, getHudFont(2));
		for (int i = 1; i < speedCount; i++) {
			float x0 = (float) ((this.widthSafe() + 1) * (i - 1) / (double) speedCount + 3);
			float y0 = (float) (100 - (speeds[i - 1] * 16) - 10);
			float x1 = (float) ((this.widthSafe() + 1) * i / (double) speedCount + 3);
			float y1 = (float) (100 - (speeds[i] * 16) - 10);
			this.drawRect(x0, y0, Math.max(0.8F, x1 - x0), Math.max(0.8F, Math.abs(y1 - y0) + 0.5F), getFontColor());
		}
		this.setWidth(155);
		this.setHeight(100);
	}

	private int widthSafe() {
		return 155;
	}

	@Override
	public String getText() {
		return "Speed: " + speedFormat.format(getSpeed()) + " m/s";
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.ACTIVITY : null;
	}

	private float getSpeed() {
		if (mc.player == null) {
			return 0;
		}
		double dx = mc.player.getX() - mc.player.prevX;
		double dz = mc.player.getZ() - mc.player.prevZ;
		return (float) (MathHelper.sqrt((float) (dx * dx + dz * dz)) / 0.05);
	}

	private void addSpeed(double speed) {
		if (speed > 3.8) {
			speed = 3.8;
		}
		System.arraycopy(speeds, 1, speeds, 0, speedCount - 1);
		speeds[speedCount - 1] = speed;
	}
}
