package me.eldodebug.confu.module.impl;

import me.eldodebug.confu.module.Category;
import me.eldodebug.confu.module.Module;
import me.eldodebug.confu.module.setting.BooleanSetting;
import me.eldodebug.confu.module.setting.NumberSetting;
import me.eldodebug.confu.util.CombatHooks;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;

/** 1.8.9 BloodParticlesMod: redstone-crack burst on the hovered entity when attacking. */
public final class BloodParticlesModule extends Module {

	private final NumberSetting amount = number("amount", "tt:AMOUNT", 2, 1, 10, 1);
	private final BooleanSetting sound = bool("sound", "tt:SOUND", true);
	private int targetId = -1;

	public BloodParticlesModule() {
		super("blood_particles", Category.RENDER, "tt:BLOOD_PARTICLES", "tt:BLOOD_PARTICLES_DESCRIPTION");
		CombatHooks.onAttack(entity -> {
			if (isEnabled()) {
				burst();
			}
		});
	}

	@Override
	public void onTick() {
		if (mc.world == null) {
			targetId = -1;
			return;
		}
		if (mc.crosshairTarget instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living) {
			targetId = living.getId();
		}
	}

	private void burst() {
		if (mc.world == null) {
			return;
		}
		Entity target = mc.world.getEntityById(targetId);
		if (!(target instanceof LivingEntity living)) {
			return;
		}
		for (int i = 0; i < amount.getInt(); i++) {
			mc.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.getDefaultState()),
					living.getX(), living.getY() + living.getHeight() - 0.75, living.getZ(), 0, 0, 0);
		}
		if (sound.enabled()) {
			mc.world.playSound(living.getX(), living.getY(), living.getZ(),
					SoundEvents.BLOCK_STONE_BREAK, SoundCategory.MASTER, 4.0F, 1.2F, false);
		}
	}
}
