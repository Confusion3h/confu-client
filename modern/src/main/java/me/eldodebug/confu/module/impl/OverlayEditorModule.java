package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;

/** 1.8.9 OverlayEditorMod. */
public final class OverlayEditorModule extends Module {

	private final BooleanSetting hidePumpkin = bool("hide_pumpkin", "tt:HIDE_PUMPKIN", false);
	private final BooleanSetting hideFire = bool("hide_fire", "tt:HIDE_FIRE", false);

	public OverlayEditorModule() {
		super("overlay_editor", Category.RENDER, "tt:OVERLAY_EDITOR", "tt:OVERLAY_EDITOR_DESCRIPTION");
	}

	public boolean hidePumpkin() {
		return isEnabled() && hidePumpkin.enabled();
	}

	public boolean hideFire() {
		return isEnabled() && hideFire.enabled();
	}
}
