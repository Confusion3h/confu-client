package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.render.PostEffect;

/** 1.8.9 ColorSaturationMod: hue, saturation, brightness and contrast post shader. */
public final class ColorSaturationModule extends Module {

	private final NumberSetting hue = number("hue", "tt:HUE", 0, 0, 1, 0.01);
	private final NumberSetting brightness = number("brightness", "tt:BRIGHTNESS", 0.5, 0, 1, 0.01);
	private final NumberSetting contrast = number("contrast", "tt:CONTRAST", 0.5, 0, 1, 0.01);
	private final NumberSetting saturation = number("saturation", "tt:SATURATION", 0.5, 0, 1, 0.01);

	private PostEffect effect;
	private boolean recreate;
	private float prevHue;
	private float prevSaturation;
	private float prevBrightness;
	private float prevContrast;
	private int prevWidth = -1;
	private int prevHeight = -1;

	public ColorSaturationModule() {
		super("color_saturation", Category.RENDER, "tt:COLOR_SATURATION", "tt:COLOR_SATURATION_DESCRIPTION");
	}

	public void renderShader() {
		float hueValue = hue.getFloat();
		float saturationValue = saturation.getFloat();
		float brightnessValue = brightness.getFloat();
		float contrastValue = contrast.getFloat();

		int scaledWidth = mc.getWindow().getScaledWidth();
		int scaledHeight = mc.getWindow().getScaledHeight();

		if (effect == null || recreate || prevWidth != scaledWidth || prevHeight != scaledHeight) {
			recreate = false;
			prevWidth = scaledWidth;
			prevHeight = scaledHeight;
			prevHue = hueValue;
			prevSaturation = saturationValue;
			prevBrightness = brightnessValue;
			prevContrast = contrastValue;
			if (effect != null) {
				effect.reset();
			}
			effect = new PostEffect("colorsaturation");
			effect.ensure();
		}

		if (prevHue != hueValue || prevSaturation != saturationValue || prevBrightness != brightnessValue || prevContrast != contrastValue) {
			effect.setUniform("hue", hueValue);
			effect.setUniform("Contrast", contrastValue);
			effect.setUniform("Brightness", brightnessValue);
			effect.setUniform("Saturation", saturationValue);
			prevHue = hueValue;
			prevSaturation = saturationValue;
			prevBrightness = brightnessValue;
			prevContrast = contrastValue;
		}

		effect.render(mc.getTickDelta());
	}

	@Override
	protected void onEnable() {
		recreate = true;
	}
}
