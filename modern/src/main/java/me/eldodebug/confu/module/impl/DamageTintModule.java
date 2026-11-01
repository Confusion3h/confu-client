package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.GameMode;

/** 1.8.9 DamageTintMod: red vignette when low HP. */
public final class DamageTintModule extends Module {

	private final NumberSetting health = number("health", "tt:HEALTH", 10, 5, 16, 1);
	private final SimpleAnimation animation = new SimpleAnimation(0);

	public DamageTintModule() {
		super("damage_tint", Category.RENDER, "tt:DAMAGE_TINT", "tt:DAMAGE_TINT_DESCRIPTION");
	}

	@Override
	public void renderHud(DrawContext context, float tickDelta) {
		if (mc.player == null || mc.interactionManager == null) {
			return;
		}
		GameMode mode = mc.interactionManager.getCurrentGameMode();
		if (mode.isCreative() || mode == GameMode.SPECTATOR) {
			return;
		}
		animation.setAnimation(mc.player.getHealth() <= health.getFloat() ? 1F : 0F, 10);
		float a = animation.getValue();
		if (a <= 0.01F) {
			return;
		}
		int w = mc.getWindow().getScaledWidth();
		int h = mc.getWindow().getScaledHeight();
		int alpha = (int) (a * 90);
		context.fillGradient(0, 0, w, h, (alpha << 24) | 0xFF0000, (alpha << 24) | 0xFF0000);
	}
}
