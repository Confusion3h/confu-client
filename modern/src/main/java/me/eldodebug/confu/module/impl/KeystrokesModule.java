package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.HudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.render.Draw;
import me.eldodebug.confu.render.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;

public final class KeystrokesModule extends HudModule {

	private static final int KEY = 28;
	private static final int GAP = 4;
	private static final int WIDTH = KEY * 3 + GAP * 2;
	private static final int ROW_HEIGHT = 22;
	private static final int RADIUS = 6;
	private static final int FORWARD = 0;
	private static final int LEFT = 1;
	private static final int BACK = 2;
	private static final int RIGHT = 3;
	private static final int JUMP = 4;
	private static final float RATE = 16F;
	private static final long MAX_FRAME_NANOS = 100_000_000L;

	private final BooleanSetting space = bool("space", "setting.keystrokes.space", true);
	private final BooleanSetting unmarked = bool("unmarked", "setting.keystrokes.unmarked", false);
	private final NumberSetting scale = number("scale", "setting.keystrokes.scale", 1, 0.5, 2, 0.05);

	private final float[] press = new float[5];
	private long lastNanos;

	public KeystrokesModule() {
		super("keystrokes", Category.HUD, "module.keystrokes.name", "module.keystrokes.desc", 0F, 0.85F);
	}

	@Override
	protected int width() {
		return WIDTH;
	}

	@Override
	protected int height() {
		return space.enabled() ? KEY + GAP + ROW_HEIGHT : KEY;
	}

	@Override
	protected float scale() {
		return scale.getFloat();
	}

	@Override
	protected void draw(DrawContext context, float tickDelta) {
		float dt = frameSeconds();
		boolean screenOpen = mc.currentScreen != null;
		GameOptions options = mc.options;

		updatePress(FORWARD, dt, !screenOpen && options.forwardKey.isPressed());
		updatePress(LEFT, dt, !screenOpen && options.leftKey.isPressed());
		updatePress(BACK, dt, !screenOpen && options.backKey.isPressed());
		updatePress(RIGHT, dt, !screenOpen && options.rightKey.isPressed());
		updatePress(JUMP, dt, !screenOpen && options.jumpKey.isPressed());

		int step = KEY + GAP;
		key(context, step, 0, "W", press[FORWARD]);
		key(context, 0, step, "A", press[LEFT]);
		key(context, step, step, "S", press[BACK]);
		key(context, step * 2, step, "D", press[RIGHT]);

		if (space.enabled()) {
			int y = step * 2;
			highlight(context, 0, y, WIDTH, ROW_HEIGHT, press[JUMP]);
			if (!unmarked.enabled()) {
				Draw.roundRect(context, WIDTH / 2 - 20, y + ROW_HEIGHT / 2 - 1, 40, 2, 1, Theme.TEXT);
			}
		}
	}

	private float frameSeconds() {
		long now = System.nanoTime();
		float dt = Math.min(MAX_FRAME_NANOS, Math.max(0L, now - lastNanos)) / 1_000_000_000F;
		lastNanos = now;
		return dt;
	}

	private void updatePress(int index, float dt, boolean down) {
		float target = down ? 1F : 0F;
		press[index] += (target - press[index]) * (1F - (float) Math.exp(-RATE * dt));
	}

	private void key(DrawContext context, int x, int y, String label, float pressed) {
		highlight(context, x, y, KEY, KEY, pressed);
		if (!unmarked.enabled()) {
			Draw.textCentered(context, mc.textRenderer, label, x + KEY / 2, y + (KEY - 8) / 2, Theme.TEXT);
		}
	}

	private void highlight(DrawContext context, int x, int y, int width, int height, float pressed) {
		Draw.roundRect(context, x, y, width, height, RADIUS, Theme.KEY);
		if (pressed > 0.01F) {
			Draw.roundRect(context, x, y, width, height, RADIUS, Theme.withAlpha(Theme.ACCENT, 0.55F * pressed));
		}
	}
}
