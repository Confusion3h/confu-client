package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.NvgHudModule;
import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.util.CombatHooks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/** 1.8.9 TargetInfoMod: last attacked living entity stats. */
public final class TargetInfoModule extends NvgHudModule {

	private LivingEntity target;
	private long hitTime;

	public TargetInfoModule() {
		super("target_info", Category.HUD, "tt:TARGET_INFO", "tt:TARGET_INFO_DESCRIPTION", 0.70F, 0.40F);
		CombatHooks.onAttack(e -> {
			if (isEnabled() && e instanceof LivingEntity living) {
				target = living;
				hitTime = System.currentTimeMillis();
			}
		});
	}

	@Override
	protected void drawNanoVG() {
		LivingEntity t = target;
		if (t == null || !t.isAlive() || System.currentTimeMillis() - hitTime > 5000) {
			if (isEditing()) {
				this.drawBackground(100, 36);
				this.drawText("Target", 5.5F, 6F, 10, getHudFont(2));
				this.drawText("20.0 HP", 5.5F, 20F, 9, getHudFont(1));
				this.setWidth(100); this.setHeight(36);
			} else {
				this.setWidth(0); this.setHeight(0);
			}
			return;
		}
		String name = t instanceof PlayerEntity p ? p.getGameProfile().getName() : t.getName().getString();
		this.drawBackground(100, 36);
		this.drawText(name, 5.5F, 6F, 10, getHudFont(2));
		this.drawText(String.format("%.1f HP", t.getHealth()), 5.5F, 20F, 9, getHudFont(1));
		this.setWidth(100);
		this.setHeight(36);
	}
}
