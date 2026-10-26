package me.eldodebug.confu.module.impl;

import java.awt.Color;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.option.KeyBinding;

/** 1.8.9 KeystrokesMod, same layout, highlight animation and labels. */
public final class KeystrokesModule extends NvgHudModule {

	private final BooleanSetting space = bool("space", "tt:SPACE", true);
	private final BooleanSetting unmarked = bool("unmarked", "tt:UNMARKED", false);
	private final SimpleAnimation[] animations = new SimpleAnimation[5];

	public KeystrokesModule() {
		super("keystrokes", Category.HUD, "tt:KEYSTROKES", "tt:KEYSTROKES_DESCRIPTION", 0F, 0.85F);
		for (int i = 0; i < 5; i++) {
			animations[i] = new SimpleAnimation();
		}
	}

	private boolean down(KeyBinding key) {
		return mc.currentScreen == null && key.isPressed();
	}

	private static String label(KeyBinding key) {
		return key.getBoundKeyLocalizedText().getString().toUpperCase();
	}

	@Override
	protected void drawNanoVG() {
		animations[0].setAnimation(down(mc.options.forwardKey) ? 1.0F : 0.0F, 16);
		animations[1].setAnimation(down(mc.options.leftKey) ? 1.0F : 0.0F, 16);
		animations[2].setAnimation(down(mc.options.backKey) ? 1.0F : 0.0F, 16);
		animations[3].setAnimation(down(mc.options.rightKey) ? 1.0F : 0.0F, 16);
		animations[4].setAnimation(down(mc.options.jumpKey) ? 1.0F : 0.0F, 16);

		this.drawBackground(32, 0, 28, 28);
		this.drawBackground(0, 32, 28, 28);
		this.drawBackground(32, 32, 28, 28);
		this.drawBackground(64, 32, 28, 28);

		highlight(32, 0, 28, 28, animations[0].getValue());
		highlight(0, 32, 28, 28, animations[1].getValue());
		highlight(32, 32, 28, 28, animations[2].getValue());
		highlight(64, 32, 28, 28, animations[3].getValue());

		if (!unmarked.enabled()) {
			this.drawCenteredText(label(mc.options.forwardKey), 32 + (28 / 2), (28 / 2) - 4, 12, getHudFont(1));
			this.drawCenteredText(label(mc.options.leftKey), 0 + (28 / 2), 32 + (28 / 2) - 4, 12, getHudFont(1));
			this.drawCenteredText(label(mc.options.backKey), 32 + (28 / 2), 32 + (28 / 2) - 4, 12, getHudFont(1));
			this.drawCenteredText(label(mc.options.rightKey), 64 + (28 / 2), 32 + (28 / 2) - 4, 12, getHudFont(1));
		}

		if (space.enabled()) {
			this.drawBackground(0, 64, (28 * 3) + 8, 22);
			highlight(0, 64, (28 * 3) + 8, 22, animations[4].getValue());
			if (!unmarked.enabled()) {
				this.drawRoundedRect(10, 74F, (26 * 3) - 6, 2, 1);
			}
		}
		this.setWidth(28 * 3 + 8);
		this.setHeight(space.enabled() ? 64 + 22 : 32 + 28);
	}

	private void highlight(float addX, float addY, float width, float height, float value) {
		this.save();
		this.scale(addX, addY, width, height, value);
		Color color = this.getFontColor((int) (120 * value));
		if (!theme().equals("rect")) {
			this.drawRoundedRect(addX, addY, width, height, 6, color);
		} else {
			this.drawRect(addX, addY, width, height, color);
		}
		this.restore();
	}
}
