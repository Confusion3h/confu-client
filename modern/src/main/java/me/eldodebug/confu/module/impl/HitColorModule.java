package me.eldodebug.confu.module.impl;

import java.awt.Color;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.soar.Glide;

/** 1.8.9 HitColorMod: hurt overlay RGB/alpha. */
public final class HitColorModule extends Module {

	private final BooleanSetting customColor = bool("custom_color", "tt:CUSTOM_COLOR", false);
	private final NumberSetting red = number("red", "tt:COLOR", 255, 0, 255, 1);
	private final NumberSetting green = number("green", "tt:COLOR", 0, 0, 255, 1);
	private final NumberSetting blue = number("blue", "tt:COLOR", 0, 0, 255, 1);
	private final NumberSetting alpha = number("alpha", "tt:ALPHA", 0.45, 0, 1, 0.05);

	public HitColorModule() {
		super("hit_color", Category.RENDER, "tt:HIT_COLOR", "tt:HIT_COLOR_DESCRIPTION");
	}

	public float[] rgbaOrNull() {
		if (!isEnabled()) {
			return null;
		}
		Color c = customColor.enabled()
				? new Color(red.getInt(), green.getInt(), blue.getInt())
				: Glide.getInstance().getColorManager().getCurrentColor().getInterpolateColor();
		return new float[] {c.getRed() / 255F, c.getGreen() / 255F, c.getBlue() / 255F, alpha.getFloat()};
	}
}
