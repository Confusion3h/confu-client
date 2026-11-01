package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;

/** 1.8.9 TabEditorMod. */
public final class TabEditorModule extends Module {

	private final BooleanSetting background = bool("background", "tt:BACKGROUND", true);
	private final BooleanSetting heads = bool("heads", "tt:HEAD", true);

	public TabEditorModule() {
		super("tab_editor", Category.RENDER, "tt:TAB_EDITOR", "tt:TAB_EDITOR_DESCRIPTION");
	}

	public boolean background() { return !isEnabled() || background.enabled(); }
	public boolean heads() { return !isEnabled() || heads.enabled(); }
}
