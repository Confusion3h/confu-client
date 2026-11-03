package me.eldodebug.soar.management.mods.settings.impl;

import java.awt.Color;

import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.settings.Setting;
import me.eldodebug.soar.utils.ColorUtils;

/** 1.8.9 ColorSetting API over a 1.20.1 ARGB module setting. */
public class ColorSetting extends Setting {

	private final me.eldodebug.confu.module.setting.ColorSetting raw;
	private float hue, saturation, brightness;
	private int alpha;

	public ColorSetting(Mod parent, me.eldodebug.confu.module.setting.ColorSetting raw) {
		super(parent, raw);
		this.raw = raw;
		sync();
	}

	private void sync() {
		Color color = getColor();
		hue = ColorUtils.getHue(color);
		saturation = ColorUtils.getSaturation(color);
		brightness = ColorUtils.getBrightness(color);
		alpha = color.getAlpha();
	}

	private void apply(Color color) {
		raw.set(color.getRGB());
	}

	/** Persist after a drag ends (avoids writing the config every frame). */
	public void commit() {
		changed();
	}

	@Override
	public void reset() {
		super.reset();
		sync();
	}

	public Color getColor() {
		return new Color(raw.argb(), true);
	}

	public void setColor(Color color) {
		apply(color);
		sync();
		changed();
	}

	public Color getDefaultColor() {
		return getColor();
	}

	public float getHue() { return hue; }
	public float getSaturation() { return saturation; }
	public float getBrightness() { return brightness; }
	public int getAlpha() { return alpha; }

	public void setHue(float hue) {
		this.hue = hue;
		apply(ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha));
	}

	public void setSaturation(float saturation) {
		this.saturation = saturation;
		apply(ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha));
	}

	public void setBrightness(float brightness) {
		this.brightness = brightness;
		apply(ColorUtils.applyAlpha(Color.getHSBColor(hue, saturation, brightness), alpha));
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
		apply(ColorUtils.applyAlpha(getColor(), alpha));
	}

	public boolean isShowAlpha() {
		return raw.showAlpha();
	}
}
