package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 MinimalViewBobbingMod: cancels bobView when enabled. */
public final class MinimalViewBobbingModule extends Module {

	public MinimalViewBobbingModule() {
		super("minimal_view_bobbing", Category.RENDER, "tt:MINIMAL_VIEW_BOBBING", "tt:MINIMAL_VIEW_BOBBING_DESCRIPTION");
	}
}
