package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.NumberSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;

/** 1.8.9 FovModifierMod: scales sprint/bow/speed/slowness FOV effects. */
public final class FovModifierModule extends Module {

	private final NumberSetting sprinting = number("sprinting", "tt:SPRINTING", 1, -5, 5, 0.1);
	private final NumberSetting bow = number("bow", "tt:BOW", 1, -5, 5, 0.1);
	private final NumberSetting speed = number("speed", "tt:SPEED", 1, -5, 5, 0.1);
	private final NumberSetting slowness = number("slowness", "tt:SLOWNESS", 1, -5, 5, 0.1);

	public FovModifierModule() {
		super("fov_modifier", Category.PLAYER, "tt:FOV_MODIFIER", "tt:FOV_MODIFIER_DESCRIPTION");
	}

	/** Extra FOV multiplier contribution (vanilla-style base around 1.0). */
	public float fovScale() {
		if (!isEnabled() || mc.player == null) {
			return 1F;
		}
		float base = 1F;
		if (mc.player.isSprinting()) {
			base += 0.15F * sprinting.getFloat();
		}
		if (mc.player.isUsingItem() && mc.player.getActiveItem().isOf(Items.BOW)) {
			int duration = Math.min(mc.player.getItemUseTime(), 20);
			float modifier = duration / 20F;
			modifier = (modifier * modifier + modifier * 2F) / 3F;
			if (modifier > 1F) {
				modifier = 1F;
			}
			base -= modifier * 0.15F * bow.getFloat();
		}
		if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
			int amp = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1;
			base += 0.1F * amp * speed.getFloat();
		}
		if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
			int amp = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() + 1;
			base += -0.075F * amp * slowness.getFloat();
		}
		return base;
	}
}
