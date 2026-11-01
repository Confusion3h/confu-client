package me.eldodebug.confu.module.impl;

import java.text.DecimalFormat;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.util.CombatHooks;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** 1.8.9 ReachDisplayMod. */
public final class ReachDisplayModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);
	private final DecimalFormat df = new DecimalFormat("0.##");
	private double distance;
	private long hitTime = -1;

	public ReachDisplayModule() {
		super("reach_display", "tt:REACH_DISPLAY", "tt:REACH_DISPLAY_DESCRIPTION", 0.15F, 0.02F);
		CombatHooks.onDamage(entity -> {
			if (!isEnabled() || mc.player == null) {
				return;
			}
			HitResult hit = mc.crosshairTarget;
			if (hit instanceof EntityHitResult ehr && ehr.getEntity() == entity && ehr.getPos() != null) {
				distance = ehr.getPos().distanceTo(mc.player.getEyePos());
				hitTime = System.currentTimeMillis();
			}
		});
	}

	@Override
	public String getText() {
		if (hitTime < 0 || System.currentTimeMillis() - hitTime > 5000) {
			distance = 0;
		}
		return distance == 0 ? "Hasn't attacked" : df.format(distance) + " blocks";
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.ACTIVITY : null;
	}
}
