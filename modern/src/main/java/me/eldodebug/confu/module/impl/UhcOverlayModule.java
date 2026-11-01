package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 UHCOverlayMod: gold/apple/skull scale overlays. */
public final class UhcOverlayModule extends Module {

	private final NumberSetting gold = number("gold", "tt:AMOUNT", 1.5, 1, 5, 0.1);
	private final NumberSetting skull = number("skull", "tt:SKULL_SCALE", 1.5, 1, 5, 0.1);

	public UhcOverlayModule() {
		super("uhc_overlay", Category.RENDER, "tt:UHC_OVERLAY", "tt:UHC_OVERLAY_DESCRIPTION");
	}

	public float goldScale() { return isEnabled() ? gold.getFloat() : 1F; }
	public float skullScale() { return isEnabled() ? skull.getFloat() : 1F; }
}
