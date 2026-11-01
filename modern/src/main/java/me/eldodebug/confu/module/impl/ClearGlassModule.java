package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;

public final class ClearGlassModule extends Module {

	private static ClearGlassModule instance;

	private final BooleanSetting normal = bool("normal", "tt:NORMAL", true);
	private final BooleanSetting stained = bool("stained", "tt:STAINED", true);

	private boolean prevNormal;
	private boolean prevStained;

	public ClearGlassModule() {
		super("clear_glass", Category.RENDER, "tt:CLEAR_GLASS", "tt:CLEAR_GLASS_DESCRIPTION");
		instance = this;
	}

	public static ClearGlassModule get() {
		return instance;
	}

	public boolean normal() {
		return normal.enabled();
	}

	public boolean stained() {
		return stained.enabled();
	}

	@Override
	public void onTick() {
		if (prevNormal != normal.enabled()) {
			prevNormal = normal.enabled();
			reloadChunks();
		}
		if (prevStained != stained.enabled()) {
			prevStained = stained.enabled();
			reloadChunks();
		}
	}

	@Override
	protected void onEnable() {
		prevNormal = normal.enabled();
		prevStained = stained.enabled();
		reloadChunks();
	}

	@Override
	protected void onDisable() {
		reloadChunks();
	}

	private void reloadChunks() {
		if (mc.worldRenderer != null) {
			mc.worldRenderer.scheduleTerrainUpdate();
		}
	}
}
