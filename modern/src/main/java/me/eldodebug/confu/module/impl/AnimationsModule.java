package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;

/** 1.8.9 AnimationsMod: old combat/sneak animation toggles. */
public final class AnimationsModule extends Module {

	private final BooleanSetting blockHit = bool("block_hit", "tt:BLOCK_HIT", true);
	private final BooleanSetting pushing = bool("pushing", "tt:PUSHING", true);
	private final BooleanSetting pushingParticle = bool("pushing_particle", "tt:PUSHING_PARTICLES", true);
	private final BooleanSetting sneak = bool("sneak", "tt:SNEAK", true);
	private final BooleanSetting smoothSneak = bool("smooth_sneak", "tt:SNEAKSMOOTH", false);
	private final NumberSetting smoothSpeed = number("smooth_speed", "tt:SMOOTH_SPEED", 6, 0.5, 20, 0.5);
	private final BooleanSetting health = bool("health", "tt:HEALTH", true);
	private final BooleanSetting armorDamage = bool("armor_damage", "tt:ARMOR_DAMAGE", false);
	private final BooleanSetting itemSwitch = bool("item_switch", "tt:ITEM_SWITCH", false);
	private final BooleanSetting rod = bool("rod", "tt:ROD", false);

	public AnimationsModule() {
		super("animations", Category.RENDER, "tt:OLD_ANIMATION", "tt:OLD_ANIMATION_DESCRIPTION");
	}

	public boolean blockHit() {
		return isEnabled() && blockHit.enabled();
	}

	public boolean pushing() {
		return isEnabled() && pushing.enabled();
	}

	public boolean pushingParticle() {
		return isEnabled() && pushingParticle.enabled();
	}

	public boolean sneak() {
		return isEnabled() && sneak.enabled();
	}

	public boolean smoothSneak() {
		return isEnabled() && sneak.enabled() && smoothSneak.enabled();
	}

	public float smoothSpeed() {
		return smoothSpeed.getFloat();
	}

	public boolean health() {
		return isEnabled() && health.enabled();
	}

	public boolean armorDamage() {
		return isEnabled() && armorDamage.enabled();
	}

	public boolean itemSwitch() {
		return isEnabled() && itemSwitch.enabled();
	}

	public boolean rod() {
		return isEnabled() && rod.enabled();
	}
}
