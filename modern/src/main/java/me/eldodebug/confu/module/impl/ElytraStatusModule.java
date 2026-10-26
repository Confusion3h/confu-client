package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.hud.SimpleNvgHudModule;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.soar.management.nanovg.font.LegacyIcon;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** New for 1.20.1: elytra durability, and speed while gliding. Hidden when no elytra is worn. */
public final class ElytraStatusModule extends SimpleNvgHudModule {

	private final BooleanSetting icon = bool("icon", "tt:ICON", true);
	private final BooleanSetting speed = bool("speed", "setting.elytra.speed", true);

	public ElytraStatusModule() {
		super("elytra_status", "module.elytra.name", "module.elytra.desc", 0F, 0.58F);
	}

	private ItemStack elytra() {
		return mc.player == null ? ItemStack.EMPTY : mc.player.getEquippedStack(EquipmentSlot.CHEST);
	}

	@Override
	protected boolean visible() {
		return elytra().isOf(Items.ELYTRA);
	}

	@Override
	public String getText() {
		ItemStack e = elytra();
		if (!e.isOf(Items.ELYTRA)) {
			return "Elytra 100%";
		}
		int left = e.getMaxDamage() - e.getDamage();
		String text = "Elytra " + Math.round(100F * left / e.getMaxDamage()) + "%";
		if (speed.enabled() && mc.player.isFallFlying()) {
			double bps = mc.player.getVelocity().length() * 20.0;
			text += " · " + String.format(java.util.Locale.ROOT, "%.1f", bps) + " b/s";
		}
		return text;
	}

	@Override
	public String getIcon() {
		return icon.enabled() ? LegacyIcon.SHIELD : null;
	}
}
