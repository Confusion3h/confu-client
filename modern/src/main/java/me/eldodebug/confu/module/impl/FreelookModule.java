package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.confu.module.setting.KeybindSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public final class FreelookModule extends Module {

	private static final float MOUSE_SCALE = 0.15F;
	private static final float SETTLED_DEGREES = 0.05F;
	private static final long MAX_FRAME_NANOS = 100_000_000L;
	private static final String MODE_HOLD = "hold";
	private static final String MODE_TOGGLE = "toggle";

	private final BooleanSetting invertYaw = bool("invert_yaw", "setting.freelook.invert_yaw", false);
	private final BooleanSetting invertPitch = bool("invert_pitch", "setting.freelook.invert_pitch", false);
	private final NumberSetting transition = number("transition", "setting.freelook.transition", 12, 2, 30, 1);
	private final ComboSetting mode = combo("mode", "setting.freelook.mode", MODE_HOLD, MODE_HOLD, MODE_TOGGLE);
	private final KeybindSetting keyBind = keybind(GLFW.GLFW_KEY_V);

	private boolean active;
	private boolean toggled;
	private boolean transitioning;
	private float freeYaw;
	private float freePitch;
	private float renderYaw;
	private float renderPitch;
	private Perspective previousPerspective;
	private long lastNanos;

	public FreelookModule() {
		super("freelook", Category.PLAYER, "tt:FREELOOK", "tt:FREELOOK_DESCRIPTION");
	}

	public boolean isActive() {
		return active;
	}

	@Override
	public void onTick() {
		boolean want;
		if (MODE_TOGGLE.equals(mode.get())) {
			if (keyPressed()) {
				toggled = !toggled;
			}
			want = toggled && mc.currentScreen == null;
		} else {
			want = keyDown();
		}

		if (want && !active) {
			start();
		} else if (!want && active) {
			stop();
		}
	}

	@Override
	protected void onDisable() {
		if (active) {
			stop();
		}
		toggled = false;
		transitioning = false;
	}

	private void start() {
		if (mc.player == null) {
			return;
		}
		active = true;
		freeYaw = mc.player.getYaw();
		freePitch = mc.player.getPitch();
		if (!transitioning) {
			renderYaw = freeYaw;
			renderPitch = freePitch;
		}
		previousPerspective = mc.options.getPerspective();
		mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
	}

	private void stop() {
		active = false;
		transitioning = true;
		if (previousPerspective != null) {
			mc.options.setPerspective(previousPerspective);
		}
	}

	/** Mouse input while freelook is engaged turns the free camera, not the character. */
	public boolean captureLook(Entity entity, double deltaX, double deltaY) {
		if (!active || entity != mc.player) {
			return false;
		}
		float yawDelta = (float) deltaX * MOUSE_SCALE;
		float pitchDelta = (float) deltaY * MOUSE_SCALE;
		if (invertYaw.enabled()) {
			yawDelta = -yawDelta;
		}
		if (invertPitch.enabled()) {
			pitchDelta = -pitchDelta;
		}
		freeYaw += yawDelta;
		freePitch = MathHelper.clamp(freePitch + pitchDelta, -90F, 90F);
		return true;
	}

	/** Camera rotation for this frame, given the character's rotation. Eases in and out over the transition speed. */
	public float[] cameraRotation(float playerYaw, float playerPitch) {
		long now = System.nanoTime();
		float dt = Math.min(MAX_FRAME_NANOS, Math.max(0L, now - lastNanos)) / 1_000_000_000F;
		lastNanos = now;

		if (!isEnabled() || (!active && !transitioning)) {
			renderYaw = playerYaw;
			renderPitch = playerPitch;
			return new float[] {playerYaw, playerPitch};
		}

		if (active && !transitioning) {
			renderYaw = freeYaw;
			renderPitch = freePitch;
			return new float[] {freeYaw, freePitch};
		}

		float targetYaw = active ? freeYaw : playerYaw;
		float targetPitch = active ? freePitch : playerPitch;
		float ease = 1F - (float) Math.exp(-transition.getFloat() * dt);
		renderYaw += MathHelper.wrapDegrees(targetYaw - renderYaw) * ease;
		renderPitch += (targetPitch - renderPitch) * ease;

		boolean settled = Math.abs(MathHelper.wrapDegrees(targetYaw - renderYaw)) < SETTLED_DEGREES
				&& Math.abs(targetPitch - renderPitch) < SETTLED_DEGREES;
		if (settled) {
			renderYaw = targetYaw;
			renderPitch = targetPitch;
			transitioning = false;
		}
		return new float[] {renderYaw, renderPitch};
	}
}
