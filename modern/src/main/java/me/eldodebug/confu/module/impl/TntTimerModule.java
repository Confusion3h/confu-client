package me.eldodebug.confu.module.impl;

import java.text.DecimalFormat;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.ComboSetting;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** 1.8.9 TNTTimerMod (HUD mode; tag mode uses world nametag via tick scan). */
public final class TntTimerModule extends SimpleNvgHudModule {

	private final ComboSetting type = combo("type", "tt:TYPE", "hud", "tag", "hud");
	private final DecimalFormat timeFormatter = new DecimalFormat("0.00");
	private TntEntity currentTnt;

	public TntTimerModule() {
		super("tnt_timer", "tt:TNT_TIMER", "tt:TNT_TIMER_DESCRIPTION", 0.30F, 0.42F);
	}

	@Override
	protected boolean visible() {
		return type.get().equals("hud");
	}

	@Override
	public String getText() {
		HitResult hit = mc.crosshairTarget;
		if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof TntEntity tnt) {
			currentTnt = tnt;
		}
		if (currentTnt != null && currentTnt.isAlive()) {
			float number = (currentTnt.getFuse() - mc.getTickDelta()) / 20.0F;
			if (number > 0) {
				return timeFormatter.format(number) + "s";
			}
			currentTnt = null;
		}
		return "There is no TNT";
	}

	public boolean tagMode() {
		return isEnabled() && type.get().equals("tag");
	}

	public String format(TntEntity tnt, float tickDelta) {
		float number = (tnt.getFuse() - tickDelta) / 20.0F;
		return number > 0 ? timeFormatter.format(number) : "";
	}
}
