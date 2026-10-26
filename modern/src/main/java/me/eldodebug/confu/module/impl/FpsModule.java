package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.TextHudModule;

public final class FpsModule extends TextHudModule {
	public FpsModule() {
		super("fps", "module.fps.name", "module.fps.desc", 0F, 0.02F);
	}

	@Override
	protected String text() {
		return mc.getCurrentFps() + " FPS";
	}
}
