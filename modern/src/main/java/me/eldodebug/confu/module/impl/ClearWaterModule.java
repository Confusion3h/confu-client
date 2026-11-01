package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 ClearWaterMod: hide water overlay / clear underwater fog. */
public final class ClearWaterModule extends Module {

	public ClearWaterModule() {
		super("clear_water", Category.RENDER, "tt:CLEAR_WATER", "tt:CLEAR_WATER_DESCRIPTION");
	}
}
