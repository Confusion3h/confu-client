package me.eldodebug.confu.module.impl;

import java.text.DecimalFormat;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** 1.8.9 HorseStatsMod. */
public final class HorseStatsModule extends NvgHudModule {

	private final DecimalFormat df = new DecimalFormat("0.0");

	public HorseStatsModule() {
		super("horse_stats", Category.HUD, "tt:HORSE_STATS", "tt:HORSE_STATS_DESCRIPTION", 0.5F, 0.20F);
	}

	@Override
	protected void drawNanoVG() {
		String speed = "Speed: 0.0 b/s";
		String jump = "Jump: 0.0 Blocks";
		HitResult hit = mc.crosshairTarget;
		if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof HorseEntity horse
				&& mc.player != null && !mc.player.hasVehicle()) {
			double base = horse.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
			speed = "Speed: " + df.format(base * 43.1703703704f) + " b/s";
			jump = "Jump: " + df.format(horse.getJumpStrength() * 5.5) + " Blocks";
		}
		this.drawBackground(95, 28);
		this.drawText(speed, 5.5F, 5.5F, 9, getHudFont(1));
		this.drawText(jump, 5.5F, 15.5F, 9, getHudFont(1));
		this.setWidth(95);
		this.setHeight(28);
	}
}
