package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.ConfuClient;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;

/** 1.8.9 Skin3DMod — voxelised skin layers, same fixed tuning values. */
public final class Skin3DModule extends Module {

	private static Skin3DModule instance;

	private final float baseVoxelSize = 1.15F;
	private final float bodyVoxelWidthSize = 1.05F;
	private final float headVoxelSize = 1.18F;

	private final int renderDistanceLOD = 14;

	public Skin3DModule() {
		super("skin_3d", Category.RENDER, "tt:SKIN_3D", "tt:SKIN_3D_DESCRIPTION");
		instance = this;
	}

	public static Skin3DModule get() {
		return instance;
	}

	@Override
	protected void onEnable() {
		MoBendsModule mobends = ConfuClient.get().modules().get(MoBendsModule.class);
		if (mobends != null && mobends.isEnabled()) {
			mobends.setEnabled(false);
		}
	}

	public float baseVoxelSize() {
		return baseVoxelSize;
	}

	public float bodyVoxelWidthSize() {
		return bodyVoxelWidthSize;
	}

	public float headVoxelSize() {
		return headVoxelSize;
	}

	public int renderDistanceLOD() {
		return renderDistanceLOD;
	}
}
