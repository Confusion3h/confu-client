package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
/** 1.8.9 HitBoxMod: force entity hitbox debug render with settings. */
public final class HitBoxModule extends Module {

	private final NumberSetting alpha = number("alpha", "tt:ALPHA", 1, 0, 1, 0.05);
	private final BooleanSetting boundingBox = bool("bounding_box", "tt:BOUNDING_BOX", true);
	private final BooleanSetting eyeHeight = bool("eye_height", "tt:EYE_HEIGHT", true);
	private final BooleanSetting lookVector = bool("look_vector", "tt:LOOK_VECTOR", true);
	private final NumberSetting lineWidth = number("line_width", "tt:LINE_WIDTH", 2, 1, 5, 1);

	public HitBoxModule() {
		super("hitbox", Category.RENDER, "tt:HITBOX", "tt:HITBOX_DESCRIPTION");
	}

	@Override
	protected void onEnable() {
		mc.getEntityRenderDispatcher().setRenderHitboxes(true);
	}

	@Override
	public void onTick() {
		if (!mc.getEntityRenderDispatcher().shouldRenderHitboxes()) {
			mc.getEntityRenderDispatcher().setRenderHitboxes(true);
		}
	}

	@Override
	protected void onDisable() {
		mc.getEntityRenderDispatcher().setRenderHitboxes(false);
	}

	public boolean boundingBox() {
		return boundingBox.enabled();
	}

	public boolean eyeHeight() {
		return eyeHeight.enabled();
	}

	public boolean lookVector() {
		return lookVector.enabled();
	}

	public float lineWidth() {
		return lineWidth.getFloat();
	}

	public float alpha() {
		return alpha.getFloat();
	}
}
