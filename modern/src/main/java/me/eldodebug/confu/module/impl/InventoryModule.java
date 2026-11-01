package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.ComboSetting;

/** 1.8.9 InventoryMod: opening animation, dim background, potion shift and particle field. */
public final class InventoryModule extends Module {

	private final BooleanSetting animation = bool("animation", "tt:ANIMATION", false);
	private final ComboSetting animationType = combo("animation_type", "tt:ANIMATION_TYPE", "normal", "normal", "backin");
	private final BooleanSetting background = bool("background", "tt:BACKGROUND", true);
	private final BooleanSetting preventPotionShift = bool("prevent_potion_shift", "tt:PREVENT_POTION_SHIFT", true);
	private final BooleanSetting particle = bool("particle", "tt:PARTICLE", false);

	public InventoryModule() {
		super("inventory", Category.OTHER, "tt:INVENTORY", "tt:INVENTORY_DESCRIPTION");
	}

	public boolean animation() {
		return isEnabled() && animation.enabled();
	}

	public boolean backIn() {
		return "backin".equals(animationType.get());
	}

	/** 1.20.1 never shifts the inventory for potion icons, so the 1.8.9 fix is inherent. */
	public boolean preventPotionShift() {
		return isEnabled() && preventPotionShift.enabled();
	}

	public boolean background() {
		return !isEnabled() || background.enabled();
	}

	public boolean particle() {
		return isEnabled() && particle.enabled();
	}
}
