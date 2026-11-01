package me.eldodebug.confu.module.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.setting.BooleanSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/** 1.8.9 PotionStatusMod. */
public final class PotionStatusModule extends NvgHudModule {

	private final BooleanSetting compact = bool("compact", "tt:COMPACT", false);
	private Collection<StatusEffectInstance> potions = List.of();
	private int maxString;
	private int prevPotionCount;

	public PotionStatusModule() {
		super("potion_status", Category.HUD, "tt:POTION_STATUS", "tt:POTION_STATUS_DESCRIPTION", 1F, 0.15F);
	}

	@Override
	public void onTick() {
		if (isEditing() || mc.player == null) {
			List<StatusEffectInstance> demo = new ArrayList<>();
			demo.add(new StatusEffectInstance(StatusEffects.SPEED, 0));
			demo.add(new StatusEffectInstance(StatusEffects.REGENERATION, 0));
			potions = demo;
		} else {
			potions = new ArrayList<>(mc.player.getStatusEffects());
		}
	}

	@Override
	protected void drawNanoVG() {
		int ySize = compact.enabled() ? 16 : 23;
		int offsetY = 16;
		if (potions.isEmpty()) {
			maxString = 0;
		}
		if (!potions.isEmpty()) {
			this.drawBackground(maxString + 29, (ySize * potions.size()) + 2);
			for (StatusEffectInstance effect : potions) {
				StatusEffect type = effect.getEffectType();
				String name = type.getName().getString();
				if (effect.getAmplifier() == 1) {
					name = name + " II";
				} else if (effect.getAmplifier() == 2) {
					name = name + " III";
				} else if (effect.getAmplifier() == 3) {
					name = name + " IV";
				}
				String time = formatDuration(effect);
				if (compact.enabled()) {
					this.drawText(name + " | " + time, 20, offsetY - 10.5F, 9, getHudFont(1));
					float totalWidth = this.getTextWidth(name + " | " + time, 9, getHudFont(1));
					if (maxString < totalWidth || prevPotionCount != potions.size()) {
						maxString = (int) totalWidth - 4;
					}
				} else {
					this.drawText(name, 25, offsetY - 12, 9, getHudFont(1));
					this.drawText(time, 25, offsetY - 1, 8, getHudFont(1));
					float levelWidth = this.getTextWidth(name, 9, getHudFont(1));
					float timeWidth = this.getTextWidth(time, 9, getHudFont(1));
					if (maxString < levelWidth || maxString < timeWidth || prevPotionCount != potions.size()) {
						maxString = (int) Math.max(levelWidth, timeWidth);
						prevPotionCount = potions.size();
					}
				}
				offsetY += ySize;
			}
		}
		this.setWidth(maxString + 29);
		this.setHeight((ySize * Math.max(2, potions.size())) + 2);
	}

	@Override
	protected void drawVanilla(DrawContext context, int px, int py, float scale) {
		if (potions.isEmpty()) {
			return;
		}
		int ySize = compact.enabled() ? 22 : 23;
		int offsetY = 16;
		MatrixStack m = context.getMatrices();
		m.push();
		m.translate(px, py, 0);
		m.scale(scale, scale, 1F);
		for (StatusEffectInstance effect : potions) {
			Identifier id = Registries.STATUS_EFFECT.getId(effect.getEffectType());
			if (id != null) {
				Identifier sprite = new Identifier(id.getNamespace(), "textures/mob_effect/" + id.getPath() + ".png");
				int ix = compact.enabled() ? 1 : 4;
				int iy = offsetY - 12;
				context.drawTexture(sprite, ix, iy, 0, 0, 18, 18, 18, 18);
			}
			offsetY += ySize;
		}
		m.pop();
	}

	private static String formatDuration(StatusEffectInstance effect) {
		int ticks = effect.getDuration();
		if (effect.isInfinite() || ticks > 20 * 60 * 60) {
			return "**:**";
		}
		int sec = Math.max(0, ticks) / 20;
		return String.format("%d:%02d", sec / 60, sec % 60);
	}
}
