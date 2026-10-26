package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.KeybindSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

public final class ZoomModule extends Module {

	private static final float WHEEL_STEP = 0.03F;
	private static final float MIN_LEVEL = 0.06F;
	private static final float MAX_LEVEL = 0.98F;
	private static final long MAX_FRAME_NANOS = 100_000_000L;

	private final BooleanSetting scroll = bool("scroll", "setting.zoom.scroll", false);
	private final BooleanSetting smoothZoom = bool("smooth", "setting.zoom.smooth", false);
	private final NumberSetting zoomSpeed = number("zoom_speed", "setting.zoom.speed", 14, 5, 20, 1);
	private final NumberSetting factor = number("factor", "setting.zoom.factor", 4, 2, 15, 1);
	private final BooleanSetting smoothCamera = bool("smooth_camera", "setting.zoom.smooth_camera", true);
	private final KeybindSetting keyBind = keybind(GLFW.GLFW_KEY_C);

	private boolean active;
	private float level = 1F;
	private float animated = 1F;
	private boolean savedSmoothCamera;
	private long lastNanos;

	public ZoomModule() {
		super("zoom", Category.PLAYER, "tt:ZOOM", "tt:ZOOM_DESCRIPTION");
	}

	@Override
	public void onTick() {
		boolean hold = keyDown();
		if (hold && !active) {
			start();
		} else if (!hold && active) {
			stop();
		}
	}

	@Override
	protected void onDisable() {
		stop();
	}

	private void start() {
		active = true;
		level = 1F / factor.getFloat();
		savedSmoothCamera = mc.options.smoothCameraEnabled;
		mc.options.smoothCameraEnabled = smoothCamera.enabled();
	}

	private void stop() {
		if (!active) {
			return;
		}
		active = false;
		level = 1F;
		mc.options.smoothCameraEnabled = savedSmoothCamera;
	}

	/** Multiplier applied to the vertical FOV for this frame. */
	public float fovMultiplier() {
		if (!isEnabled()) {
			return 1F;
		}
		float target = active ? level : 1F;
		if (!smoothZoom.enabled()) {
			animated = target;
			return target;
		}
		long now = System.nanoTime();
		float dt = Math.min(MAX_FRAME_NANOS, Math.max(0L, now - lastNanos)) / 1_000_000_000F;
		lastNanos = now;
		animated += (target - animated) * (1F - (float) Math.exp(-zoomSpeed.getFloat() * dt));
		return animated;
	}

	/** Returns true when the wheel input belongs to zoom and must not reach the hotbar. */
	public boolean onScroll(double vertical) {
		if (!active || !scroll.enabled() || vertical == 0D) {
			return false;
		}
		if (vertical < 0D) {
			if (level < MAX_LEVEL) {
				level += WHEEL_STEP;
			}
		} else if (level > MIN_LEVEL) {
			level -= WHEEL_STEP;
		}
		return true;
	}
}
