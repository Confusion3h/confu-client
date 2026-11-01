package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 FPSLimiterMod: overrides max FPS / GUI FPS. */
public final class FpsLimiterModule extends Module {

	private final BooleanSetting limitMax = bool("limit_max", "tt:LIMIT_MAX_FPS", true);
	private final NumberSetting maxFps = number("max_fps", "tt:MAX_FPS", 480, 240, 1440, 1);
	private final BooleanSetting limitGui = bool("limit_gui", "tt:LIMIT_GUI_FPS", true);
	private final NumberSetting guiFps = number("gui_fps", "tt:GUI_FPS", 30, 1, 240, 1);

	public FpsLimiterModule() {
		super("fps_limiter", Category.OTHER, "tt:FPS_LIMITER", "tt:FPS_LIMITER_DESCRIPTION");
	}

	@Override
	public void onTick() {
		if (!isEnabled()) {
			return;
		}
		if (limitGui.enabled() && mc.currentScreen != null) {
			mc.getWindow().setFramerateLimit(guiFps.getInt());
		} else if (limitMax.enabled()) {
			mc.getWindow().setFramerateLimit(maxFps.getInt());
		}
	}

	@Override
	protected void onDisable() {
		mc.getWindow().setFramerateLimit(mc.options.getMaxFps().getValue());
	}
}
