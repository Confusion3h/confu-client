package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 ImageDisplayMod: placeholder panel (custom image load later). */
public final class ImageDisplayModule extends NvgHudModule {

	private final NumberSetting width = number("width", "tt:WIDTH", 100, 40, 300, 1);
	private final NumberSetting height = number("height", "tt:HEIGHT", 60, 20, 200, 1);

	public ImageDisplayModule() {
		super("image_display", Category.HUD, "tt:IMAGE_DISPLAY", "tt:IMAGE_DISPLAY_DESCRIPTION", 0.80F, 0.50F);
	}

	@Override
	protected void drawNanoVG() {
		int w = width.getInt(), h = height.getInt();
		this.drawBackground(w, h);
		this.drawCenteredText("Image", w / 2F, h / 2F - 4, 9, getHudFont(1));
		this.setWidth(w);
		this.setHeight(h);
	}
}
