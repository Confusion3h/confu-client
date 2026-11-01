package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.render.PostEffect;
import me.eldodebug.confu.util.Sound;

/** 1.8.9 MotionBlurMod: shader trail or the legacy glAccum accumulation blur. */
public final class MotionBlurModule extends Module {

	private final ComboSetting type = combo("type", "tt:TYPE", "SHADER", "ACCUM", "SHADER");
	private final NumberSetting amount = number("amount", "tt:AMOUNT", 0.5, 0.1, 0.9, 0.05);

	private PostEffect effect;
	private boolean recreate;
	private float groupBlur;
	private boolean loaded;
	private int prevWidth = -1;
	private int prevHeight = -1;

	public MotionBlurModule() {
		super("motion_blur", Category.RENDER, "tt:MOTION_BLUR", "tt:MOTION_BLUR_DESCRIPTION");
	}

	public boolean isShaderMode() {
		return "SHADER".equals(type.get());
	}

	public void renderShader() {
		if (!isShaderMode()) {
			return;
		}

		float amountValue = amount.getFloat();
		int scaledWidth = mc.getWindow().getScaledWidth();
		int scaledHeight = mc.getWindow().getScaledHeight();

		if (effect == null || recreate || prevWidth != scaledWidth || prevHeight != scaledHeight) {
			recreate = false;
			prevWidth = scaledWidth;
			prevHeight = scaledHeight;
			groupBlur = amountValue;
			if (effect != null) {
				effect.reset();
			}
			effect = new PostEffect("motion_blur");
			effect.ensure();
		}

		if (groupBlur != amountValue || !loaded) {
			loaded = true;
			effect.setUniform("BlurFactor", amountValue);
			groupBlur = amountValue;
		}

		effect.render(mc.getTickDelta());
	}

	public void onUpdateDisplay() {
		if (!"ACCUM".equals(type.get())) {
			return;
		}

		if (effect != null) {
			effect.reset();
			effect = null;
		}
		recreate = false;
		loaded = false;

		if (mc.player == null) {
			return;
		}

		// 1.20.1 core profile has no accumulation buffer; glAccum aborts the JVM natively,
		// so take the 1.8.9 error path instead: disable the module and play its error sound.
		setEnabled(false);
		try {
			Sound.play("soar/audio/error.wav", false);
		} catch (Exception ignored) {
		}
	}

	@Override
	protected void onEnable() {
		recreate = true;
	}
}
