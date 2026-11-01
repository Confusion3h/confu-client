package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;

/** 1.8.9 PotionCounterMod: healing potions in inventory. */
public final class PotionCounterModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);

	public PotionCounterModule() {
		super("potion_counter", "tt:POTION_COUNTER", "tt:POTION_COUNTER_DESCRIPTION", 0.15F, 0.66F);
	}

	@Override
	public String getText() {
		int amount = countHealPots();
		return amount + " " + (amount <= 1 ? "pot" : "pots");
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.ARCHIVE : null;
	}

	private int countHealPots() {
		if (mc.player == null) {
			return 0;
		}
		int count = 0;
		for (int i = 0; i < mc.player.getInventory().size(); i++) {
			ItemStack stack = mc.player.getInventory().getStack(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (!(stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION))) {
				continue;
			}
			boolean heal = PotionUtil.getPotionEffects(stack).stream()
					.anyMatch(e -> e.getEffectType() == StatusEffects.INSTANT_HEALTH);
			if (heal) {
				count += stack.getCount();
			}
		}
		return count;
	}
}
