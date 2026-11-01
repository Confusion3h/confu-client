package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.module.impl.rearview.RearviewCamera;
import me.eldodebug.soar.utils.TimerUtils;

/** 1.8.9 RearviewMod — mirror-cam HUD element fed by RearviewCamera's re-entrant world render. */
public final class RearviewModule extends NvgHudModule {

	private static RearviewModule instance;

	private final RearviewCamera camera = new RearviewCamera();
	private final TimerUtils timer = new TimerUtils();

	private final NumberSetting mirrorWidth = number("width", "tt:WIDTH", 190, 10, 500, 1);
	private final NumberSetting mirrorHeight = number("height", "tt:HEIGHT", 100, 10, 500, 1);
	private final NumberSetting fps = number("fps", "tt:FPS", 60, 1, 120, 1);
	private final NumberSetting fov = number("fov", "tt:FOV", 70, 30, 120, 1);
	private final BooleanSetting lockCamera = bool("lock_camera", "tt:LOCK_CAMERA", true);
	private final NumberSetting alpha = number("alpha", "tt:ALPHA", 1, 0, 1, 0.01);

	public RearviewModule() {
		super("rearview", Category.HUD, "tt:REARVIEW", "tt:REARVIEW_DESCRIPTION", 0.5F, 0.1F);
		instance = this;
	}

	public static RearviewModule get() {
		return instance;
	}

	/** 1.8.9 onRenderTick — one mirror render per fps budget tick. */
	public void onRenderTick(float tickDelta) {
		if (mc.world == null) {
			return;
		}
		if (timer.delay((long) (1000 / fps.getFloat()))) {
			camera.updateMirror(tickDelta);
			timer.reset();
		}
	}

	@Override
	protected void drawNanoVG() {
		int width = (int) (mirrorWidth.getFloat() * getScale());
		int height = (int) (mirrorHeight.getFloat() * getScale());

		camera.setFov(fov.getFloat());
		camera.setLockCamera(lockCamera.enabled());

		nvg().drawShadow(getX(), getY(), width, height, 6 * getScale());
		if (camera.getTexture() != 0) {
			nvg().drawRoundedImage(camera.getTexture(), getX(), getY() + height, width, -height, 6 * getScale(), alpha.getFloat());
		}

		setWidth((int) (width / getScale()));
		setHeight((int) (height / getScale()));
	}
}
