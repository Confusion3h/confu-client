package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.util.CombatHooks;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;

/** 1.8.9 ComboCounterMod. */
public final class ComboCounterModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);
	private long hitTime = -1;
	private int combo;
	private int possibleTarget = -1;

	public ComboCounterModule() {
		super("combo_counter", "tt:COMBO_COUNTER", "tt:COMBO_COUNTER_DESCRIPTION", 0.15F, 0.34F);
		CombatHooks.onAttack(entity -> {
			if (isEnabled()) {
				possibleTarget = entity.getId();
			}
		});
		CombatHooks.onDamage(entity -> {
			if (!isEnabled() || mc.player == null) {
				return;
			}
			if (entity.getId() == possibleTarget) {
				combo++;
				possibleTarget = -1;
				hitTime = System.currentTimeMillis();
			} else if (entity == mc.player) {
				combo = 0;
			}
		});
	}

	@Override
	public void onTick() {
		if (hitTime >= 0 && System.currentTimeMillis() - hitTime > 2000) {
			combo = 0;
		}
	}

	@Override
	public String getText() {
		return combo == 0 ? "No Combo" : combo + " Combo";
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.BAR_CHERT : null;
	}
}
