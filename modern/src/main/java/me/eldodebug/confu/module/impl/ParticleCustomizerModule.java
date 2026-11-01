package me.eldodebug.confu.module.impl;

import java.util.Random;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.util.CombatHooks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;

/** 1.8.9 ParticleCustomizerMod: extra sharpness/crit particles on hit. */
public final class ParticleCustomizerModule extends Module {

	private final BooleanSetting alwaysSharpness = bool("always_sharpness", "tt:ALWAYS_SHARPNESS", false);
	private final BooleanSetting alwaysCriticals = bool("always_criticals", "tt:ALWAYS_CRITICALS", false);
	private final BooleanSetting sharpness = bool("sharpness", "tt:SHARPNESS", true);
	private final BooleanSetting criticals = bool("criticals", "tt:CRITICALS", false);
	private final NumberSetting sharpnessAmount = number("sharpness_amount", "tt:SHARPNESS_AMOUNT", 2, 1, 10, 1);
	private final NumberSetting criticalsAmount = number("criticals_amount", "tt:CRITICALS_AMOUNT", 2, 1, 10, 1);
	private final Random random = new Random();

	public ParticleCustomizerModule() {
		super("particle_customizer", Category.RENDER, "tt:PARTICLE_CUSTOMIZER", "tt:PARTICLE_CUSTOMIZER_DESCRIPTION");
		CombatHooks.onAttack(entity -> {
			if (isEnabled()) {
				onAttack(entity);
			}
		});
	}

	private void onAttack(Entity entity) {
		if (!(entity instanceof LivingEntity) || mc.player == null || mc.world == null) {
			return;
		}
		PlayerEntity player = mc.player;
		boolean critical = criticals.enabled() && player.fallDistance > 0.0F && !player.isOnGround()
				&& !player.isClimbing() && !player.isTouchingWater()
				&& !player.hasStatusEffect(StatusEffects.BLINDNESS) && player.getVehicle() == null;
		if (critical || alwaysCriticals.enabled()) {
			spawn(entity, ParticleTypes.CRIT, criticalsAmount.getInt() - 1);
		}
		boolean sharp = sharpness.enabled() && sharpnessLevel(player.getMainHandStack()) > 0;
		if (alwaysSharpness.enabled() || sharp) {
			spawn(entity, ParticleTypes.ENCHANTED_HIT, sharpnessAmount.getInt() - 1);
		}
	}

	private static int sharpnessLevel(ItemStack stack) {
		return EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack)
				+ EnchantmentHelper.getLevel(Enchantments.SMITE, stack)
				+ EnchantmentHelper.getLevel(Enchantments.BANE_OF_ARTHROPODS, stack);
	}

	private void spawn(Entity entity, DefaultParticleType particle, int count) {
		if (count <= 0) {
			return;
		}
		Box box = entity.getBoundingBox();
		for (int i = 0; i < count; i++) {
			double x = box.minX + random.nextDouble() * box.getXLength();
			double y = box.minY + random.nextDouble() * box.getYLength();
			double z = box.minZ + random.nextDouble() * box.getZLength();
			double vx = (random.nextDouble() - 0.5) * 0.4;
			double vy = (random.nextDouble() - 0.5) * 0.4;
			double vz = (random.nextDouble() - 0.5) * 0.4;
			mc.world.addParticle(particle, x, y, z, vx, vy, vz);
		}
	}
}
