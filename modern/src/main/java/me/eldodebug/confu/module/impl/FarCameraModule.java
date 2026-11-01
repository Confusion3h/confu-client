package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 FarCameraMod: third-person camera distance. */
public final class FarCameraModule extends Module {

	private final NumberSetting range = number("range", "tt:RANGE", 15, 0, 50, 1);

	public FarCameraModule() {
		super("far_camera", Category.RENDER, "tt:FAR_CAMERA", "tt:FAR_CAMERA_DESCRIPTION");
	}

	public double distance(double vanilla) {
		return isEnabled() ? range.getFloat() : vanilla;
	}
}
