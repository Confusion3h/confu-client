package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 MouseStrokesMod: trailing mouse-path HUD. */
public final class MouseStrokesModule extends NvgHudModule {

	private final NumberSetting size = number("size", "tt:WIDTH", 64, 32, 128, 1);
	private final float[] pointsX = new float[40];
	private final float[] pointsY = new float[40];
	private int idx;
	private double lastMx = Double.NaN, lastMy;

	public MouseStrokesModule() {
		super("mouse_strokes", Category.HUD, "tt:MOUSE_STROKES", "tt:MOUSE_STROKES_DESCRIPTION", 0.85F, 0.70F);
	}

	@Override
	public void onTick() {
		double mx = mc.mouse.getX();
		double my = mc.mouse.getY();
		if (!Double.isNaN(lastMx)) {
			float dx = (float) (mx - lastMx);
			float dy = (float) (my - lastMy);
			pointsX[idx] = dx;
			pointsY[idx] = dy;
			idx = (idx + 1) % pointsX.length;
		}
		lastMx = mx;
		lastMy = my;
	}

	@Override
	protected void drawNanoVG() {
		int s = size.getInt();
		this.drawBackground(s, s);
		float cx = s / 2F, cy = s / 2F;
		float px = cx, py = cy;
		for (int i = 0; i < pointsX.length; i++) {
			int j = (idx + i) % pointsX.length;
			float nx = px + pointsX[j] * 0.05F;
			float ny = py + pointsY[j] * 0.05F;
			this.drawRect(nx, ny, 1.5F, 1.5F, getFontColor(80 + i * 4));
			px = nx;
			py = ny;
		}
		this.setWidth(s);
		this.setHeight(s);
	}
}
