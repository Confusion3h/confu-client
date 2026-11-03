package me.eldodebug.confu.module.impl;

import java.awt.Color;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;
import me.eldodebug.soar.Glide;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/** 1.8.9 ModernHotbarMod: themed hotbar with four designs and smooth selection. */
public final class ModernHotbarModule extends NvgHudModule {

	private final ComboSetting design = combo("design", "tt:DESIGN", "client", "normal", "soar", "chill", "client");
	private final BooleanSetting smooth = bool("smooth", "tt:SMOOTH", true);
	private final ComboSetting pickup = combo("pickup_anim", "tt:PICKUP_ANIM", "pop", "pop", "bread", "vanilla");
	private final SimpleAnimation animation = new SimpleAnimation(0F);

	public ModernHotbarModule() {
		super("modern_hotbar", Category.HUD, "tt:MODERN_HOTBAR", "tt:MODERN_HOTBAR_DESCRIPTION", 0.5F, 1F);
	}

	public boolean chill() {
		return isEnabled() && design.get().equals("chill");
	}

	@Override
	protected void drawNanoVG() {
		int screenWidth = mc.getWindow().getScaledWidth();
		int screenHeight = mc.getWindow().getScaledHeight();
		boolean chill = design.get().equals("chill");
		float barX = chill ? 0 : screenWidth / 2F - 91F;
		float barY = chill ? screenHeight - 22F : screenHeight - 26F;
		float barWidth = chill ? screenWidth : 182F;
		float barHeight = 22F;

		this.setOrigin(barX, barY);
		AccentColor currentColor = Glide.getInstance().getColorManager().getCurrentColor();

		switch (design.get()) {
			case "soar" -> {
				nvg().drawShadow(barX, barY, barWidth, barHeight, 6);
				nvg().drawGradientRoundedRect(barX, barY, barWidth, barHeight, 6,
						ColorUtils.applyAlpha(currentColor.getColor1(), 190), ColorUtils.applyAlpha(currentColor.getColor2(), 190));
			}
			case "chill" -> {
				nvg().drawShadow(barX, barY, barWidth, barHeight, 0);
				this.drawRect(0, 0, barWidth, barHeight, new Color(20, 20, 20, 180));
			}
			case "client" -> {
				if (theme().equals("text")) {
					nvg().drawShadow(barX, barY, barWidth, barHeight, 6);
				}
				this.drawBackground(0, 0, barWidth, barHeight, 6);
			}
			default -> {
				nvg().drawShadow(barX, barY, barWidth, barHeight, 6);
				nvg().drawRoundedRect(barX, barY, barWidth, barHeight, 6, new Color(0, 0, 0, 100));
			}
		}

		int slot = mc.player == null ? 0 : mc.player.getInventory().selectedSlot;
		float target = slot * 20F;
		if (smooth.enabled()) {
			animation.setAnimation(target, 18);
			target = animation.getValue();
		}

		if (chill) {
			this.drawRect(target + 1, 0, 22, 22, new Color(230, 230, 230, 180));
		} else if (design.get().equals("soar")) {
			this.drawRoundedRect(target + 1, 0, 22, 22, 6, new Color(255, 255, 255, 140));
		} else {
			this.drawRoundedRect(target + 1, 0, 22, 22, 6, new Color(0, 0, 0, 100));
		}

		this.setWidth((int) barWidth);
		this.setHeight((int) barHeight);
	}

	@Override
	protected void drawVanilla(DrawContext context, int px, int py, float scale) {
		if (mc.player == null) {
			return;
		}
		int screenWidth = mc.getWindow().getScaledWidth();
		int screenHeight = mc.getWindow().getScaledHeight();
		boolean chill = design.get().equals("chill");
		int baseX = screenWidth / 2 - 90 + 2;
		int baseY = chill ? screenHeight - 19 : screenHeight - 23;

		for (int i = 0; i < 9; i++) {
			ItemStack stack = mc.player.getInventory().getStack(i);
			if (!stack.isEmpty()) {
				int x = baseX + i * 20;
				// 1.8.9 ModernHotbarMod#renderHotBarItem pickup animations (pop / bread / vanilla)
				String style = pickup.get();
				float tickDelta = mc.getTickDelta();
				float progress = stack.getBobbingAnimationTime() - ("bread".equals(style) ? tickDelta / 2F : tickDelta);
				boolean animate = progress > 0F;
				if (animate) {
					context.getMatrices().push();
					context.getMatrices().translate(x + 8, baseY + 12, 0F);
					if ("bread".equals(style)) {
						float s = 1F + progress / 2.5F;
						context.getMatrices().scale(Math.max(1F, s / (1F / (s / 2F))), s, 1F);
					} else if ("pop".equals(style)) {
						float s = 1F + progress / 5F;
						context.getMatrices().scale(s, s, 1F);
					} else {
						float s = 1F + progress / 5F;
						context.getMatrices().scale(1F / s, (s + 1F) / 2F, 1F);
					}
					context.getMatrices().translate(-(x + 8), -(baseY + 12), 0F);
				}
				context.drawItem(stack, x, baseY);
				if (animate) {
					context.getMatrices().pop();
				}
				context.drawItemInSlot(mc.textRenderer, stack, x, baseY);
			}
		}
	}

	public String pickupStyle() {
		return pickup.get();
	}
}
