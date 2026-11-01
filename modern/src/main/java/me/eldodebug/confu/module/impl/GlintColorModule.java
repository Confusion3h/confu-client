package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.soar.Glide;

/** 1.8.9 GlintColorMod: enchantment glint tint. */
public final class GlintColorModule extends Module {

	private final NumberSetting alpha = number("alpha", "tt:ALPHA", 1, 0, 1, 0.05);

	public GlintColorModule() {
		super("glint_color", Category.RENDER, "tt:GLINT_COLOR", "tt:GLINT_COLOR_DESCRIPTION");
	}

	public float[] rgbaOrNull() {
		if (!isEnabled()) return null;
		var c = Glide.getInstance().getColorManager().getCurrentColor().getInterpolateColor();
		return new float[]{c.getRed()/255F, c.getGreen()/255F, c.getBlue()/255F, alpha.getFloat()};
	}
}
